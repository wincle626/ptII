/* Base class for code generators.

 Copyright (c) 2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.kernel;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** Base class for code generator.
 *
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributors: Christopher Brooks
 *  @version $Id$ CodeGenerator.java,v 1.51 2005/07/13 14:07:20 cxh Exp $
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {
    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/codegen");
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");
        
        inline = new Parameter(this, "inline");
        inline.setTypeEquals(BaseType.BOOLEAN);
        inline.setExpression("true");

        overwriteFiles = new Parameter(this, "overwriteFiles");
        overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
        overwriteFiles.setExpression("true");       
        
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;

    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;
    
    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the 
     *  value true.
     */
    public Parameter inline;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/". Subclasses may override this
     *  produce comments that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        return "/* " + comment + " */\n";
    }

    /** Generate the body code that lies between initialize and wrapup.
     *  In this base class, nothing is generated.
     *  @return The empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateBodyCode() throws IllegalActionException {
        return "";
    }

    /** Generate code and write it to the file specified by the
     *  <i>codeDirectory</i> parameter.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public void generateCode() throws KernelException {
        generateCode(new StringBuffer());
    }

    /** Generate code and append it to the given string buffer.
     *  Write the code to the file specified by the codeDirectory parameter.
     *  This is the main entry point.
     *  @param code The given string buffer.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public void generateCode(StringBuffer code) throws KernelException {
        
        boolean inline = ((BooleanToken) this.inline.getToken()).booleanValue();
        
        // perform port type conversion. This has to be before generation
        // of other codes.
        _checkPortTypeConversion();        

        // We separate the generation and the appending into 2 phases.
        // This would be convenience for making addition passes, and
        // for adding additional code into different sections.
        String sharedCode = generateSharedCode();
        String includeFiles = generateIncludeFiles();
        String preinitializeCode = generatePreinitializeCode();
        String variableDeclareCode = generateVariableDeclaration(); 
        String initializeCode = generateInitializeCode();
        String bodyCode = generateBodyCode();
        String fireFunctionCode = null;;
        if (!inline) {
            fireFunctionCode = generateFireFunctionCode();         
        }
        String wrapupCode = generateWrapupCode();
        
        // generate type resolution code has to be after 
        // fire(), wrapup(), preinit(), init()...
        String typeResolutionCode = generateTypeResolutionCode();

        // The appending phase.
        code.append(includeFiles);
        code.append(sharedCode);
        code.append(typeResolutionCode);
        if (!inline) {
            code.append(fireFunctionCode);         
        }
        code.append("\n\nmain(int argc, char *argv[]) {\n");
        code.append(variableDeclareCode);
        code.append(preinitializeCode);
        code.append(initializeCode);
        code.append(bodyCode);
        code.append(wrapupCode);
        code.append("}\n");

        // Write the code to the file specified by codeDirectory.
        try {
            // Check if needs to overwrite.
            if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                    && codeDirectory.asFile().exists()) {
                // FIXME: It is totally bogus to ask a yes/no question
                // like this, since it makes it impossible to call
                // this method from a script.  If the question is
                // asked, the build will hang.
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }

            Writer writer = codeDirectory.openForWriting();
            writer.write(code.toString());
            codeDirectory.close();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeDirectory.asFile() + "\"");
        }
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a 
     *  function with the same name as that of the actor.
     * 
     *  @return The fire function code of the containing composite actor.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeEntity model = (CompositeEntity) getContainer();
        TypedCompositeActor modelHelper = (TypedCompositeActor) _getHelper(model);
                     
        code.append("\nvoid " + 
                model.getFullName().replace('.' , '_') + "() {\n");
        code.append(modelHelper.generateFireCode());
        code.append("}\n");
        return code.toString();
    }
    
    /** Generate include files.
     *  @return The include files.
     *  @throws IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
            code.append("#include " + file + "\n");
        }

        return code.toString();
    }

    /**
     * Return the code associated with initialization of the containing
     * composite actor. This method calls the generateInitializeCode()
     * method of the code generator helper associated with the model director.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If the helper class for the model
     *  director cannot be found or if an error occurs when the director
     *  helper generates initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Initialize " + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateInitializeCode());
        return code.toString();
    }

    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator helper associated with the model director
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();

        CompositeEntity model = (CompositeEntity) getContainer();

        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + model.getName() + " does not have a director.");
        }
        
        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());
        
        _modifiedVariables = compositeActorHelper.getModifiedVariables();
        
        code.append(compositeActorHelper.generatePreinitializeCode());
        
        code.append(compositeActorHelper.createOffsetVariablesIfNeeded());

        Attribute iterations = director.getAttribute("iterations");

        if (iterations != null) {
            int iterationCount = ((IntToken) 
                    ((Variable) iterations).getToken()).intValue();

            if (iterationCount > 0) {
                code.append("static int iteration = 0;\n");
            }
        }

        return code.toString();
    }

    /**
     * Generate code shared by helper actors, including globally defined
     * data struct types and static methods or variables shared by multiple
     * instances of the same helper actor type.
     * @return The shared code of the containing composite actor.
     * @throws IllegalActionException If an error ocurrs when generating
     *  the globally shared code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the shared code.
     */
    public String generateSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Generate shared code for "
                + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());

        Set sharedCodeBlocks = compositeActorHelper.generateSharedCode();

        Iterator blocks = sharedCodeBlocks.iterator();

        while (blocks.hasNext()) {
            String block = (String) blocks.next();
            code.append(block);
        }

        code.append(comment("Finished generate shared code for "
                + getContainer().getFullName()));

        return code.toString();
    }

    /**
     * Generate type resolution code.
     * Determine the proper code put into the source to support dynamic type
     * resolution. First, find out the different types used in the model.
     * Second, find out the different polymorphic functions used. (note: types
     * and functions are independent of each other). Third, append code blocks
     * according to the functions used, and read from files according to the
     * types referenced. Fourth, generate type resolution code, which consists
     * of constants (MAX_NUM_TYPE, MAX_NUM_FUNC), the type map, the function
     * map, function definitions read from the files, and function table.
     * @return The type resolution code.
     * @throws IllegalActionException If an error ocurrs when generating
     *  the type resolution code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the type resolution code.
     */
    public String generateTypeResolutionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Generate type resolution code for "
                + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());

        Iterator actors = ((ptolemy.actor.CompositeActor) compositeActorHelper
                .getComponent()).deepEntityList().iterator();

        CodeStream sharedStream = new CodeStream(
        "$CLASSPATH/ptolemy/codegen/kernel/SharedCode.c");
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());
        
        // Determine the total number of referenced types.
        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();        
        HashSet types = new HashSet();
        types.addAll(_primitiveTypes);

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            CodeGeneratorHelper helperObject = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            functions.addAll(helperObject._typeFuncUsed);
            types.addAll(helperObject._newTypesUsed);
        }
        // The constructor of Array requires calling the convert function.  
        if (types.contains("Array")) {
        	functions.add("convert");
        }
        
        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        String typeMembers = new String();
        for (int i = 0; i < types.size(); i++) {
            // Open the .c file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/kernel/type/" + typesArray[i]
                            + ".c");
            // FIXME: we need to compute the [partial] order of the hierarchy. 
            code.append("#define TYPE_" + typesArray[i] + " " + i + "\n");

            // Dynamically generate all the types within the union.
            typeMembers += "\t\t" + typesArray[i] + "Token " + typesArray[i]
                    + ";\n";
        }

        Object[] functionsArray = functions.toArray();

        // Generate function map.
        for (int i = 0; i < functions.size(); i++) {
            code.append("#define FUNC_" + functionsArray[i] + " " + i + "\n");
        }
        code.append("typedef struct token Token;");
        
        // Generate type and function definitions.
        for (int i = 0; i < types.size(); i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            code.append(typeStreams[i].toString());
        }

        // Token declareBlock.
        if (!typeMembers.equals("")) {
            ArrayList args = new ArrayList();
            args.add(typeMembers);
            sharedStream.clear();
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);
            code.append(sharedStream.toString());
        }

        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("funcDeclareBlock");
            code.append(typeStreams[i].toString());
        }

        // FIXME: in the future we need to load the convertPrimitivesBlock
        // dynamically, and maybe break it into multiple blocks to minimize
        // code size.
        sharedStream.clear();
        sharedStream.appendCodeBlock("convertPrimitivesBlock");
        code.append(sharedStream.toString());
        
        // Generate function table.
        if (functions.size() > 0 && types.size() > 0) {
            code.append("#define NUM_TYPE " + types.size() + "\n");
            code.append("#define NUM_FUNC " + functions.size() + "\n");
            code.append("Token (*functionTable" +
                    "[NUM_TYPE][NUM_FUNC])(Token)= {\n");
            for (int i = 0; i < types.size(); i++) {
                code.append("\t");
                for (int j = 0; j < functions.size(); j++) {
                    code.append(typesArray[i] + "_" + functionsArray[j]);
                    if ((i != (types.size() - 1)) || 
                        (j != (functions.size() - 1))) {
                        code.append(", ");
                    }
                }
                code.append("\n");
            }
            code.append("};\n");
        }
        
        for (int i = 0; i < types.size(); i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("newBlock");

            for (int j = 0; j < functions.size(); j++) {
                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/ 
                //     .....
                // /**/
                try {
                    typeStreams[i].appendCodeBlock(functionsArray[j] + "Block");
                } catch (IllegalActionException ex) {
                    // We have to catch the exception if some code blocks are
                    // not found. We have to define the function label in the
                    // generated code because the function table makes
                    // reference to this label.
                    typeStreams[i].append("#define " + typesArray[i] + "_"
                            + functionsArray[j] + " MISSING \n");

                    // It is ok because this polymorphic function may not be
                    // supported by all types. 
                }
            }
            code.append(typeStreams[i].toString());
        }
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("\n\n");
        code.append(comment("Variable Declarations "
                + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());

        code.append(compositeActorHelper.generateVariableDeclaration());
        return code.toString();
    }

    /** Generate into the specified code stream the code associated with
     *  wrapping up the container composite actor. This method calls the
     *  generateWrapupCode() method of the code generator helper associated
     *  with the director of this container.
     *  @return The wrapup code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Wrapup " + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = 
            (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateWrapupCode());
        return code.toString();
    }

    /** Return the associated component, which is always the container.
     *  @return The helper to generate code.
     */
    public NamedObj getComponent() {
        return getContainer();
    }

    /** Generate code for a model.
     *  <p>For example:
     *  <pre>
     *  java -classpath $PTII ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  or
     *  <pre>
     *  $PTII/bin/ptinvoke ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @exception Exception If any error occurs.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.codegen.kernel.CodeGenerator model.xml "
                    + "[model.xml . . .]\n"
                    + "  The arguments name MoML files containing models");
        }

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        parser.setMoMLFilters(BackwardCompatibility.allFilters());
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {
            // Note: the code below uses explicit try catch blocks
            // so we can provide very clear error messages about what
            // failed to the end user.  The alternative is to wrap the
            // entire body in one try/catch block and say
            // "Code generation failed for foo", which is not clear.
            URL modelURL;

            try {
                modelURL = new File(args[i]).toURL();
            } catch (Exception ex) {
                throw new Exception("Could not open \"" + args[i] + "\"", ex);
            }

            CompositeActor toplevel = null;

            try {
                try {
                    toplevel = (CompositeActor) parser.parse(null, modelURL);
                } catch (Exception ex) {
                    throw new Exception("Failed to parse \"" + args[i] + "\"",
                            ex);
                }

                // Get all instances of this class contained in the model
                List codeGenerators = toplevel
                        .attributeList(CodeGenerator.class);

                CodeGenerator codeGenerator;

                if (codeGenerators.size() == 0) {
                    // Add a codeGenerator
                    codeGenerator = new CodeGenerator(toplevel,
                            "CodeGenerator_AutoAdded");
                } else {
                    // Get the last CodeGenerator in the list, maybe
                    // it was added last?
                    codeGenerator = (CodeGenerator) codeGenerators
                            .get(codeGenerators.size() - 1);
                }

                System.out.println("CodeGenerator: " + codeGenerator);

                try {
                    codeGenerator.generateCode();
                } catch (KernelException ex) {
                    throw new Exception("Failed to generate code for \""
                            + args[i] + "\"", ex);
                }
            } finally {
                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                }
            }
        }
    }

    /** Th method is used to set the code generator for a helper class.
     *  Since this is not a helper class for a component, this method does
     *  nothing.
     *  @param codeGenerator
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
    }

    /** Set the container of this object to be the given container.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeEntity.
     *  @exception NameDuplicationException If there already exists a
     *   container with the same name.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if ((container != null) && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                            + " by CompositeEntity");
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Check for port type conversion between actors and record the conversion
     * needed in the helper's info table. The helper would knows how to convert
     * its input ports from the recorded information. If source ports and the
     * helper's input ports are of different types, then conversion is needed.
     * (i.e. if a source port is a primitive type and the input port is a 
     * general type, we need to upgrade the source type to a Token type.) 
     * @exception Thrown if helper is not found, or the conversion not
     *  supported.
     */
    protected void _checkPortTypeConversion() throws IllegalActionException {        

        Iterator actors = ((ptolemy.actor.CompositeActor) _getHelper(
                getContainer()).getComponent()).deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper helper = 
                (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            
            for (int i = 0; i < actor.inputPortList().size(); i++) {
            	TypedIOPort inputPort = 
                    ((TypedIOPort) actor.inputPortList().get(i));

                // j is the source port channel index in the input port.
                for (int j = 0; j < inputPort.sourcePortList().size(); j++) {
            		TypedIOPort sourcePort = 
                        ((TypedIOPort) inputPort.sourcePortList().get(j));
                    
                    if (!inputPort.getType().equals(sourcePort.getType())) {
                        String sourceType = CodeGeneratorHelper.
                        _getCodeGenTypeFromPtolemyType(sourcePort.getType());
                        String targetType = CodeGeneratorHelper.
                        _getCodeGenTypeFromPtolemyType(inputPort.getType());

                        CodeGeneratorHelper sourceHelper = (CodeGeneratorHelper) 
                        _getHelper(sourcePort.getContainer());
                        
                        // Record the needed inter-actor type conversions.
                        // **Source port reference name is uniquely identified
                        // by "[NAME]#[INDEX]", its name and its channel#.
                        String refName = sourcePort.getName();
                        refName += "#" + sourceHelper._getChannelIndex(
                                inputPort, j, sourcePort);
                        
                        if (targetType.equals("Token")) {
                            // Record the referenced type in the infoTable.
                            sourceHelper._newTypesUsed.add(sourceType);
                            sourceHelper._portConversions.put(refName, sourceType + "_new");
                            sourceHelper._portDeclareTypes.put(refName, "Token");
                        } else if (targetType.equals("String")) {
                            if (sourceType.equals("Int")) {
                                sourceHelper._portConversions.put(refName, "itoa");
                            } else if (sourceType.equals("Long")) {
                                sourceHelper._portConversions.put(refName, "ltoa");
                            } else if (sourceType.equals("Double")) {
                                sourceHelper._portConversions.put(refName, "ftoa");            
                            } else if (sourceType.equals("Boolean")) {
                                sourceHelper._portConversions.put(refName, "btoa");                                
                            } else {
                                throw new IllegalActionException(
                                    "Port type conversion not handled -- from "
                                    + sourceType + " to " + targetType + ".\n");                                
                            }
                            sourceHelper._portDeclareTypes.put(refName, "char*");
                        } else if (targetType.equals("Double")) {
                            if (sourceType.equals("Int")) {
                                // C would take care of converting
                                // from int to double.
                            } else {
                                throw new IllegalActionException(
                                    "Port type conversion not handled -- from "
                                    + sourceType + " to " + targetType + ".\n");                                
                            }
                        } else if (targetType.equals("Array")) {
                        	
                        } else {
                        	// FIXME: we may have to handle other port conversion types.
                            //throw new IllegalActionException(
                            //    "Port type conversion not handled -- from "
                            //    + sourceType + " to " + targetType + ".\n");
                            throw new IllegalActionException(
                                    "Port type conversion not handled -- from "
                                    + sourceType + " to " + targetType + ".\n");                                
                        }                        
                    } 
                }
            }
        }
    }
    
    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        if (_helperStore.containsKey(component)) {
            return (ComponentCodeGenerator) _helperStore.get(component);
        }

        String packageName = generatorPackage.stringValue();

        String componentClassName = component.getClass().getName();
        String helperClassName = componentClassName.replaceFirst("ptolemy",
                packageName);

        Class helperClass = null;

        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Cannot find helper class " + helperClassName);
        }

        Constructor constructor = null;

        try {
            constructor = helperClass.getConstructor(new Class[] { component
                    .getClass() });
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " + helperClassName
                            + " which accepts an instance of "
                            + componentClassName + " as the argument.");
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component });
        } catch (Exception e) {
            throw new IllegalActionException((NamedObj) component, e,
                    "Failed to create helper class code generator.");
        }

        if (!(helperObject instanceof ComponentCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: " + component
                            + ". Its helper class does not"
                            + " implement componentCodeGenerator.");
        }

        ComponentCodeGenerator castHelperObject = 
            (ComponentCodeGenerator) helperObject;

        castHelperObject.setCodeGenerator(this);

        _helperStore.put(component, helperObject);

        return castHelperObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** A set that contains all variables in the model whose values can be 
     *  changed during execution.
     */
    protected Set _modifiedVariables;

    /** 
     * A static list of all primitive types supported by the code generator. 
     */
    protected static List _primitiveTypes = Arrays.asList(new String[] {
            "Int", "Double", "String", "Boolean"});
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    private HashMap _helperStore = new HashMap();
}
