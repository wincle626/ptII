/* Utilities used to manipulate strings.

 Copyright (c) 2002-2006 The Regents of the University of California.
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
package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.
import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// StringUtilities

/**
 A collection of utilities for manipulating strings.
 These utilities do not depend on any other ptolemy packages.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class StringUtilities {
    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Abbreviate a string.
     *  If the string is longer than 80 characters, truncate it by
     *  displaying the first 37 chars, then ". . .", then the last 38
     *  characters.
     *  If the <i>longName</i> argument is null, then the string
     *  "&gt;Unnamed&lt;" is returned.
     *  @param longName The string to be abbreviated.
     *  @return The possibly abbreviated name.
     *  @see #split(String)
     */
    public static String abbreviate(String longName) {
        // This method is used to abbreviate window titles so that long
        // file names may appear in the window title bar.  It is not
        // parameterized so that we can force a unified look and feel.
        // FIXME: it would be nice to split on a nearby space. 
        if (longName == null) {
            return "<Unnamed>";
        }

        if (longName.length() <= 80) {
            return longName;
        }

        return longName.substring(0, 37) + ". . ."
                + longName.substring(longName.length() - 38);
    }

    /** Return a string with a maximum line length of <i>length</i>
     *  characters, limited to the given number of characters.
     *  If there are more than 10 newlines, then the string is truncated
     *  after 10 lines.
     *  If the string is truncated, an ellipsis (three periods in a
     *  row: "...") will be appended to the end of the string.
     *  Lines that are longer than 160 characters are split into lines
     *  that are shorter than 160 characters.
     *  @param string The string to truncate.
     *  @param length The number of characters to which to truncate the string.
     *  @return The possibly truncated string with ellipsis possibly added.
     */
    public static String ellipsis(String string, int length) {
        // If necessary, insert newlines into long strings.
        // If we don't do split long lines and we throw an exception
        // with a very long line, then the window close button and
        // possible the dismiss button will be off the right side of
        // the screen.
        // The number 160 was generated by trying different sizes and
        // seeing what fits on a 1024 wide screen.
        string = StringUtilities.split(string, 160);

        // Third argument being true means return the delimiters as tokens.
        StringTokenizer tokenizer = new StringTokenizer(string, LINE_SEPARATOR,
                true);

        // If there are more than 42 lines and 42 newlines, return
        // truncate after the first 42 lines and newlines.
        // This is necessary so that we can deal with very long lines
        // of text without spaces.
        if (tokenizer.countTokens() > 42) {
            StringBuffer results = new StringBuffer();

            for (int i = 0; (i < 42) && tokenizer.hasMoreTokens(); i++) {
                results.append(tokenizer.nextToken());
            }

            results.append("...");
            string = results.toString();
        }

        if (string.length() > length) {
            return string.substring(0, length - 3) + "...";
        }

        return string;
    }

    /** Given a string, replace all the instances of XML special characters
     *  with their corresponding XML entities.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.
     *
     *  <p>In this method, we make the following translations:
     *  <pre>
     *  &amp; becomes &amp;amp;
     *  " becomes &amp;quot;
     *  < becomes &amp;lt;
     *  > becomes &amp;gt;
     *  newline becomes &amp;#10;
     *  carriage return becomes $amp;#13;
     *  </pre>
     *  @see #unescapeForXML(String)
     *
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     */
    public static String escapeForXML(String string) {
        string = substitute(string, "&", "&amp;");
        string = substitute(string, "\"", "&quot;");
        string = substitute(string, "<", "&lt;");
        string = substitute(string, ">", "&gt;");
        string = substitute(string, "\n", "&#10;");
        string = substitute(string, "\r", "&#13;"); // Carriage return
        return string;
    }

    /** If the ptolemy.ptII.exitAfterWrapup property is not set, then 
     *  call System.exit().
     *  Ptolemy code should call this method instead of directly calling
     *  System.exit() so that we can test code that would usually exit.
     *  @param returnValue The return value of this process, where
     *  non-zero values indicate an error.
     */
    public static void exit(int returnValue) {
        // $PTII/util/testsuite/testDefs.tcl sets
        // ptolemy.ptII.exitAfterWrapup
        if (StringUtilities.getProperty("ptolemy.ptII.exitAfterWrapup")
                .length() > 0) {
            throw new RuntimeException("Normally, we would "
                    + "exit here because Manager.exitAfterWrapup() "
                    + "was called.  However, because the "
                    + "ptolemy.ptII.exitAfterWrapup property "
                    + "is set, we throw this exception instead.");
        } else {
            // Non-zero indicates a problem.
            System.exit(returnValue);
        }
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    public static String getIndentPrefix(int level) {
        if (level <= 0) {
            return "";
        }

        StringBuffer result = new StringBuffer(level * 4);

        for (int i = 0; i < level; i++) {
            result.append("    ");
        }

        return result.toString();
    }

    /** Get the specified property from the environment. An empty
     *  string is returned if the property named by the "propertyName"
     *  argument environment variable does not exist, though if
     *  certain properties are not defined, then we make various
     *  attempts to determine them and then set them.  See the javadoc
     *  page for java.util.System.getProperties() for a list of system
     *  properties.

     *  <p>The following properties are handled specially
     *  <dl>
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.  However, if we are running under Web Start,
     *  then this property might not be set, in which case we look
     *  for "ptolemy/util/StringUtilities.class" and set the
     *  property accordingly.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> Return $PTII as a URL.  For example, if $PTII was c:\ptII,
     *  then return file:/c:/ptII/.
     *  <dt> "user.dir"
     *  <dd> Return the canonical path name to the current working
     *  directory.  This is necessary because under Windows with
     *  JDK1.4.1, the System.getProperty() call returns
     *  <code><b>c</b>:/<i>foo</i></code> whereas most of the other
     *  methods that operate on path names return
     *  <code><b>C</b>:/<i>foo</i></code>.
     *  </dl>
     *  @param propertyName The name of property.
     *  @return A String containing the string value of the property.
     *  If the property is not found, then we return the empty string.
     */
    public static String getProperty(String propertyName) {
        // NOTE: getProperty() will probably fail in applets, which
        // is why this is in a try block.
        String property = null;

        try {
            property = System.getProperty(propertyName);
        } catch (SecurityException ex) {
            if (!propertyName.equals("ptolemy.ptII.dir")) {
                // Constants.java depends on this when running with
                // -sandbox.
                SecurityException security = new SecurityException(
                        "Could not find '" + propertyName + "' System property");
                security.initCause(ex);
                throw security;
            }
        }

        if (propertyName.equals("user.dir")) {
            try {
                File userDirFile = new File(property);
                return userDirFile.getCanonicalPath();
            } catch (IOException ex) {
                return property;
            }
        }

        // Check for cases where the ptII property starts with
        // the string "/cygdrive".  This can happen if the property
        // was set by doing "PTII=`pwd`" under Cygwin bash.
        if (property != null) {
            if (propertyName.equals("ptolemy.ptII.dir")
                    && property.startsWith("/cygdrive")
                    && !_printedCygwinWarning) {
                // This error only occurs when users build their own,
                // so it is safe to print to stderr
                _printedCygwinWarning = true;
                System.err.println("ptolemy.ptII.dir property = \"" + property
                        + "\", which contains \"cygdrive\". "
                        + "This is almost always an error under Cygwin that "
                        + "is occurs when one does PTII=`pwd`.  Instead, do "
                        + "PTII=c:/foo/ptII");
            }

            return property;
        }

        if (propertyName.equals("ptolemy.ptII.dirAsURL")) {
            // Return $PTII as a URL.  For example, if $PTII was c:\ptII,
            // then return file:/c:/ptII/
            File ptIIAsFile = new File(getProperty("ptolemy.ptII.dir"));

            try {
                URL ptIIAsURL = ptIIAsFile.toURL();
                return ptIIAsURL.toString();
            } catch (java.net.MalformedURLException malformed) {
                throw new RuntimeException("While trying to find '"
                        + propertyName + "', could not convert '" + ptIIAsFile
                        + "' to a URL", malformed);
            }
        }

        if (propertyName.equals("ptolemy.ptII.dir")) {
            if (_ptolemyPtIIDir != null) {
                // Return the previously calculated value
                return _ptolemyPtIIDir;
            } else {
                String stringUtilitiesPath = "ptolemy/util/StringUtilities.class";

                // PTII variable was not set
                URL namedObjURL = Thread.currentThread()
                        .getContextClassLoader().getResource(
                                stringUtilitiesPath);

                if (namedObjURL != null) {
                    // Get the file portion of URL
                    String namedObjFileName = namedObjURL.getFile();

                    // FIXME: How do we get from a URL to a pathname?
                    if (namedObjFileName.startsWith("file:")) {
                        if (namedObjFileName.startsWith("file:/")
                                || namedObjFileName.startsWith("file:\\")) {
                            // We get rid of either file:/ or file:\
                            namedObjFileName = namedObjFileName.substring(6);
                        } else {
                            // Get rid of file:
                            namedObjFileName = namedObjFileName.substring(5);
                        }
                    }

                    String abnormalHome = namedObjFileName.substring(0,
                            namedObjFileName.length()
                                    - stringUtilitiesPath.length());

                    // abnormalHome will have values like: "/C:/ptII/"
                    // which cause no end of trouble, so we construct a File
                    // and call toString().
                    _ptolemyPtIIDir = (new File(abnormalHome)).toString();

                    // If we are running under Web Start, then strip off
                    // the trailing "!"
                    if (_ptolemyPtIIDir.endsWith("/!")
                            || _ptolemyPtIIDir.endsWith("\\!")) {
                        _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                _ptolemyPtIIDir.length() - 1);
                    }

                    // Web Start, we might have
                    // RMptsupport.jar or
                    // XMptsupport.jar1088483703686
                    String ptsupportJarName = File.separator + "DMptolemy"
                            + File.separator + "RMptsupport.jar";

                    if (_ptolemyPtIIDir.endsWith(ptsupportJarName)) {
                        _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                _ptolemyPtIIDir.length()
                                        - ptsupportJarName.length());
                    } else {
                        ptsupportJarName = "/DMptolemy/XMptsupport.jar";

                        if (_ptolemyPtIIDir.lastIndexOf(ptsupportJarName) != -1) {
                            _ptolemyPtIIDir = _ptolemyPtIIDir.substring(0,
                                    _ptolemyPtIIDir
                                            .lastIndexOf(ptsupportJarName));
                        }
                    }
                }

                // Convert %20 to spaces because if a URL has %20 in it,
                // then we know we have a space, but file names do not
                // recognize %20 as being a single space, instead file names
                // see %20 as three characters: '%', '2', '0'.
                if (_ptolemyPtIIDir != null) {
                    _ptolemyPtIIDir = StringUtilities.substitute(
                            _ptolemyPtIIDir, "%20", " ");
                }

                if (_ptolemyPtIIDir == null) {
                    throw new RuntimeException("Could not find "
                            + "'ptolemy.ptII.dir'" + " property.  "
                            + "Also tried loading '" + stringUtilitiesPath
                            + "' as a resource and working from that. "
                            + "Vergil should be "
                            + "invoked with -Dptolemy.ptII.dir" + "=\"$PTII\"");
                }

                try {
                    // Here, we set the property so that future updates
                    // will get the correct value.
                    System.setProperty("ptolemy.ptII.dir", _ptolemyPtIIDir);
                } catch (SecurityException security) {
                    // Ignore, we are probably running as an applet or -sandbox
                }

                return _ptolemyPtIIDir;
            }
        }

        // If the property is not set then we return the empty string. 
        if (property == null) {
            return "";
        }

        return property;
    }

    /** Merge the properties in lib/ptII.properties with the current
     *  properties.  lib/ptII.properties is searched for in the
     *  classpath.  The value of properties listed in
     *  lib/ptII.properties do not override properties with the same
     *  name in the current properties.
     *  @exception IOException If thrown while looking for the 
     *  $CLASSPATH/lib/ptII.properties file.
     */
    public static void mergePropertiesFile() throws IOException {
        Properties systemProperties = System.getProperties();
        Properties newProperties = new Properties();
        String propertyFileName = "$CLASSPATH/lib/ptII.properties";

        // FIXME: xxxxxxCLASSPATHxxxxxx is an ugly hack
        URL propertyFileURL = FileUtilities.nameToURL(
                "xxxxxxCLASSPATHxxxxxx/lib/ptII.properties", null, null);

        if (propertyFileURL == null) {
            throw new IOException("Could not find " + propertyFileName);
        }

        newProperties.load(propertyFileURL.openStream());

        // systemProperties is a HashSet, so we merge in the new properties.
        newProperties.putAll(systemProperties);
        System.setProperties(newProperties);
        // FIXME: This should be logged, not printed.
        //System.out.println("Loaded " + propertyFileURL);
    }

    /** Return a string representing the name of the file expected to
     *  contain the source code for the specified object.  This method
     *  simply replaces "." with "/" and appends ".java" to the class
     *  name.
     *  @param object The object.
     *  @return The expected source file name.
     */
    public static String objectToSourceFileName(Object object) {
        String sourceFileNameBase = object.getClass().getName().replace('.',
                '/');

        // Inner classes: Get rid of everything past the first $
        if (sourceFileNameBase.indexOf("$") != -1) {
            sourceFileNameBase = sourceFileNameBase.substring(0,
                    sourceFileNameBase.indexOf("$"));
        }

        return sourceFileNameBase + ".java";
    }

    /** Return the preferences directory, creating it if necessary.
     *  @return A string naming the preferences directory.  The last
     *  character of the string will have the file.separator character
     *  appended.
     *  @exception IOException If the directory could not be created.
     *  @see #PREFERENCES_DIRECTORY
     */
    public static String preferencesDirectory() throws IOException {
        String preferencesDirectoryName = StringUtilities
                .getProperty("user.home")
                + File.separator
                + StringUtilities.PREFERENCES_DIRECTORY
                + File.separator;
        File preferencesDirectory = new File(preferencesDirectoryName);

        if (!preferencesDirectory.isDirectory()) {
            if (preferencesDirectory.mkdirs() == false) {
                throw new IOException("Could not create user preferences "
                        + "directory '" + preferencesDirectoryName + "'");
            }
        }

        return preferencesDirectoryName;
    }

    /** Return the name of the properties file.
     *  The properties file is a file of a format suitable for
     *  java.util.properties.load(InputStream).
     *  The file is named "ptII.properties" and is found in the
     *  {@link #PREFERENCES_DIRECTORY} directory that is returned
     *  by {@link #preferencesDirectory()}.  Typically, this value
     *  is "$HOME/.ptolemyII/ptII.properties".
     *  @see #preferencesDirectory()
     *  @see #PREFERENCES_DIRECTORY
     *  @return The name of the properties file.
     *  @exception IOException If {@link #preferencesDirectory()} throws it.
     */
    public static String propertiesFileName() throws IOException {
        return preferencesDirectory() + "ptII.properties";
    }

    /** Sanitize a String so that it can be used as a Java identifier.
     *  Section 3.8 of the Java language spec says:
     *  <blockquote>
     *  "An identifier is an unlimited-length sequence of Java letters
     *  and Java digits, the first of which must be a Java letter. An
     *  identifier cannot have the same spelling (Unicode character
     *  sequence) as a keyword (3.9), boolean literal (3.10.3), or
     *  the null literal (3.10.7)."
     *  </blockquote>
     *  Java characters are A-Z, a-z, $ and _.
     *  <p> Characters that are not permitted in a Java identifier are changed
     *  to underscores.
     *  This method does not check that the returned string is a
     *  keyword or literal.
     *  Note that two different strings can sanitize to the same
     *  string.
     *  This method is commonly used during code generation to map the
     *  name of a ptolemy object to a valid identifier name.
     *  @param name A string with spaces and other characters that
     *  cannot be in a Java name.
     *  @return A String that follows the Java identifier rules.
     */
    public static String sanitizeName(String name) {
        char[] nameArray = name.toCharArray();

        for (int i = 0; i < nameArray.length; i++) {
            if (!Character.isJavaIdentifierPart(nameArray[i])) {
                nameArray[i] = '_';
            }
        }

        if (nameArray.length == 0) {
            return "";
        } else {
            if (!Character.isJavaIdentifierStart(nameArray[0])) {
                return "_" + new String(nameArray);
            } else {
                return new String(nameArray);
            }
        }
    }

    /**  If the string is longer than 79 characters, split it up by
     *  adding newlines in all newline delimited substrings
     *  that are longer than 79 characters.
     *  If the <i>longName</i> argument is null, then the string
     *  "&gt;Unnamed&lt;" is returned.
     *  @see #abbreviate(String)
     *  @see #split(String, int)
     *  @param longName The string to optionally split up
     *  @return Either the original string, or the string with newlines
     *  inserted.
     */
    public static String split(String longName) {
        return split(longName, 79);
    }

    /** If the string is longer than <i>length</i> characters,
     *  split the string up by adding newlines in all
     *  newline delimited substrings that are longer than <i>length</i>
     *  characters.
     *  If the <i>longName</i> argument is null, then the string
     *  "&gt;Unnamed&lt;" is returned.
     *  @see #abbreviate(String)
     *  @see #split(String)
     *  @param longName The string to optionally split.
     *  @param length The maximum length of the sequence of characters
     *  before a newline is inserted.
     *  @return Either the original string, or the string with newlines
     *  inserted.
     */
    public static String split(String longName, int length) {
        if (longName == null) {
            return "<Unnamed>";
        }

        if (longName.length() <= length) {
            return longName;
        }

        StringBuffer results = new StringBuffer();

        // The third argument is true, which means return the delimiters
        // as part of the tokens.
        StringTokenizer tokenizer = new StringTokenizer(longName,
                LINE_SEPARATOR, true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int mark = 0;

            while (mark < (token.length() - length)) {
                // We look for the space from the end of the first length
                // characters.  If we find one, then we use that
                // as the place to insert a newline.
                int lastSpaceIndex = token.substring(mark, mark + length)
                        .lastIndexOf(" ");

                if (lastSpaceIndex < 0) {
                    // No space found, just insert a new line after length
                    results.append(token.substring(mark, mark + length)
                            + LINE_SEPARATOR);
                    mark += length;
                } else {
                    results.append(token.substring(mark, mark + lastSpaceIndex)
                            + LINE_SEPARATOR);
                    mark += (lastSpaceIndex + 1);
                }
            }

            results.append(token.substring(mark));
        }

        return results.toString();
    }

    /** Given a file or URL name, return as a URL.  If the file name
     *  is relative, then it is interpreted as being relative to the
     *  specified base directory. If the name begins with
     *  "xxxxxxCLASSPATHxxxxxx" or "$CLASSPATH"
     *  then search for the file relative to the classpath.
     *  Note that this is the value of the globally defined constant
     *  $CLASSPATH available in the expression language.
     *  If no file is found, then throw an exception.
     *  @param name The name of a file or URL.
     *  @param baseDirectory The base directory for relative file names,
     *   or null to specify none.
     *  @param classLoader The class loader to use to locate system
     *   resources, or null to use the system class loader.
     *  @return A URL, or null if no file name or URL has been specified.
     *  @exception IOException If the file cannot be read, or
     *   if the file cannot be represented as a URL (e.g. System.in), or
     *   the name specification cannot be parsed.
     *  @exception MalformedURLException If the URL is malformed.
     *  @deprecated Use FileUtilities.nameToURL instead.
     */
    public static URL stringToURL(String name, URI baseDirectory,
            ClassLoader classLoader) throws IOException {
        return FileUtilities.nameToURL(name, baseDirectory, classLoader);
    }

    /** Replace all occurrences of <i>pattern</i> in the specified
     *  string with <i>replacement</i>.  Note that the pattern is NOT
     *  a regular expression, and that relative to the
     *  String.replaceAll() method in jdk1.4, this method is extremely
     *  slow.  This method does not work well with back slashes.
     *  @param string The string to edit.
     *  @param pattern The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string, String pattern,
            String replacement) {
        int start = string.indexOf(pattern);

        while (start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + pattern.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(pattern, start + replacement.length());
        }

        return string;
    }

    /** Perform file prefix substitution.
     *  If <i>string</i> starts with <i>prefix</i>, then we return a
     *  new string that consists of the value or <i>replacement</i>
     *  followed by the value of <i>string</i> with the value of
     *  <i>prefix</i> removed.  For example,
     *  substituteFilePrefix("c:/ptII", "c:/ptII/ptolemy, "$PTII")
     *  will return "$PTII/ptolemy"
     *
     *  <p>If <i>prefix</i> is not a simple prefix of <i>string</i>, then
     *  we use the file system to find the canonical names of the files.
     *  For this to work, <i>prefix</i> and <i>string</i> should name
     *  files that exist, see java.io.File.getCanonicalFile() for details.
     *
     *  <p>If <i>prefix</i> is not a prefix of <i>string</i>, then
     *  we return <i>string</i>
     *
     *  @param prefix The prefix string, for example, "c:/ptII".
     *  @param string The string to be substituted, for example,
     *  "c:/ptII/ptolemy".
     *  @param replacement The replacement to be substituted in, for example,
     *  "$PTII"
     *  @return The possibly substituted string.
     */
    public static String substituteFilePrefix(String prefix, String string,
            String replacement) {
        // This method is currently used by $PTII/util/testsuite/auto.tcl
        if (string.startsWith(prefix)) {
            // Hmm, what about file separators?
            return replacement + string.substring(prefix.length());
        } else {
            try {
                String prefixCanonicalPath = (new File(prefix))
                        .getCanonicalPath();

                String stringCanonicalPath = (new File(string))
                        .getCanonicalPath();

                if (stringCanonicalPath.startsWith(prefixCanonicalPath)) {
                    return replacement
                            + stringCanonicalPath.substring(prefixCanonicalPath
                                    .length());
                }
            } catch (Throwable throwable) {
                // ignore.
            }
        }

        return string;
    }

    /** Tokenize a String to an array of Strings for use with
     *  Runtime.exec(String []).
     *
     *  <p>Lines that begin with an octothorpe '#' are ignored.
     *  Substrings that start and end with a double quote are considered
     *  to be a single token and are returned as a single array element.
     *
     *  @param inputString  The String to tokenize
     *  @return An array of substrings.
     *  @exception IOException If StreamTokenizer.nextToken() throws it.
     */
    public static String[] tokenizeForExec(String inputString)
            throws IOException {
        // The java.lang.Runtime.exec(String command) call uses
        // java.util.StringTokenizer() to parse the command string.
        // Unfortunately, this means that double quotes are not handled
        // in the same way that the shell handles them in that 'ls "foo
        // 'bar"' will interpreted as three tokens 'ls', '"foo' and
        // 'bar"'.  In the shell, the string would be two tokens 'ls' and
        // '"foo bar"'.  What is worse is that the exec() behaviour is
        // slightly different under Windows and Unix.  To solve this
        // problem, we preprocess the command argument using
        // java.io.StreamTokenizer, which converts quoted substrings into
        // single tokens.  We then call java.lang.Runtime.exec(String []
        // commands);
        // Parse the command into tokens
        List commandList = new LinkedList();

        StreamTokenizer streamTokenizer = new StreamTokenizer(new StringReader(
                inputString));

        // We reset the syntax so that we don't convert to numbers,
        // otherwise, if PTII is "d:\\tmp\\ptII\ 2.0", then
        // we have no end of problems.
        streamTokenizer.resetSyntax();
        streamTokenizer.whitespaceChars(0, 32);
        streamTokenizer.wordChars(33, 127);

        // We can't use quoteChar here because it does backslash
        // substitution, so "c:\ptII" ends up as "c:ptII"
        // Substituting forward slashes for backward slashes seems like
        // overkill.
        // streamTokenizer.quoteChar('"');
        streamTokenizer.ordinaryChar('"');

        streamTokenizer.eolIsSignificant(true);

        streamTokenizer.commentChar('#');

        // Current token
        String token = "";

        // Single character token, usually a -
        String singleToken = "";

        // Set to true if we are inside a double quoted String.
        boolean inDoubleQuotedString = false;

        while (streamTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            switch (streamTokenizer.ttype) {
            case StreamTokenizer.TT_WORD:

                if (inDoubleQuotedString) {
                    if (token.length() > 0) {
                        // FIXME: multiple spaces will get compacted here
                        token += " ";
                    }

                    token += (singleToken + streamTokenizer.sval);
                } else {
                    token = singleToken + streamTokenizer.sval;
                    commandList.add(token);
                }

                singleToken = "";
                break;

            case StreamTokenizer.TT_NUMBER:
                throw new RuntimeException("Internal error: Found TT_NUMBER: '"
                        + streamTokenizer.nval + "'.  We should not be "
                        + "tokenizing numbers");

            //break;
            case StreamTokenizer.TT_EOL:
                break;

            case StreamTokenizer.TT_EOF:
                break;

            default:
                singleToken = Character.toString((char) streamTokenizer.ttype);

                if (singleToken.equals("\"")) {
                    if (inDoubleQuotedString) {
                        commandList.add(token);
                    }

                    inDoubleQuotedString = !inDoubleQuotedString;
                    singleToken = "";
                    token = "";
                }

                break;
            }
        }

        return (String[]) commandList.toArray(new String[commandList.size()]);
    }

    /** Given a string, replace all the instances of XML entities
     *  with their corresponding XML special characters.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.
     *
     *  <p>In this method, we make the following translations:
     *  <pre>
     *  &amp;amp; becomes &amp
     *  &amp;quot; becomes "
     *  &amp;lt; becomes <
     *  &amp;gt; becomes >
     *  &amp;#10; becomes newline
     *  &amp;#13; becomes carriage return
     *  </pre>
     *  @see #escapeForXML(String)
     *
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     */
    public static String unescapeForXML(String string) {
        string = substitute(string, "&amp;", "&");
        string = substitute(string, "&quot;", "\"");
        string = substitute(string, "&lt;", "<");
        string = substitute(string, "&gt;", ">");
        string = substitute(string, "&#10;", "\n");
        string = substitute(string, "&#13;", "\r");
        return string;
    }

    /** Return a string that contains a description of how to use a
     *  class that calls this method.  For example, this method is
     *  called by "$PTII/bin/vergil -help".
     *  @param commandTemplate  A string naming the command and the
     *  format of the arguments, for example
     *  "moml [options] [file . . .]"
     *  @param commandOptions A 2xN array of Strings that list command-line
     *  options that take arguments where the first
     *  element is a String naming the command line option, and the
     *  second element is the argument, for example
     *  <code>{"-class", "<classname>")</code>
     *  @param commandFlags An array of Strings that list command-line
     *  options that are either present or not.
     *  @return A string that describes the command.
     */
    public static String usageString(String commandTemplate,
            String[][] commandOptions, String[] commandFlags) {
        // This method is static so that we can reuse it in places
        // like copernicus/kernel/Copernicus and actor/gui/MoMLApplication
        StringBuffer result = new StringBuffer("Usage: " + commandTemplate
                + "\n\n" + "Options that take values:\n");

        int i;

        for (i = 0; i < commandOptions.length; i++) {
            result.append(" " + commandOptions[i][0] + " "
                    + commandOptions[i][1] + "\n");
        }

        result.append("\nBoolean flags:\n");

        for (i = 0; i < commandFlags.length; i++) {
            result.append(" " + commandFlags[i]);
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // If you change these, be sure to try running vergil on
    // a HSIF moml file
    // vergil ../hsif/demo/SwimmingPool/SwimmingPool.xml

    /** Maximum length in characters of a long string before
     *  {@link #ellipsis(String, int)} truncates and add a
     *  trailing ". . .".  This variable is used by callers
     *  of ellipsis(String, int).
     */
    public static final int ELLIPSIS_LENGTH_LONG = 2000;

    /** Maximum length in characters of a short string before
     *  {@link #ellipsis(String, int)} truncates and add a
     *  trailing ". . .". This variable is used by callers
     *  of ellipsis(String, int).
     */
    public static final int ELLIPSIS_LENGTH_SHORT = 400;

    /** The line separator string.  Under Windows, this would
     *  be "\r\n"; under Unix, "\n"; Under Macintosh, "\r".
     */
    public static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    /** Location of Application preferences such as the user library.
     *  This field is not final in case other applications want to
     *  set it to a different directory.
     *  @see #preferencesDirectory()
     */
    public static String PREFERENCES_DIRECTORY = ".ptolemyII";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set to true if we print the cygwin warning in getProperty(). */
    private static boolean _printedCygwinWarning = false;

    /** Cached value of ptolemy.ptII.dir property. */
    private static String _ptolemyPtIIDir = null;
}
