/*
An implementation of the visitor design pattern that generates C code
from Jimple statements.

Copyright (c) 2001-2002 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

//////////////////////////////////////////////////////////////////////////
//// CSwitch
/*
   An implementation of the visitor design pattern that generates C code
   from Jimple statements. Code generated in this class is placed
   in an internal code stack (see {@link #_push(StringBuffer)},
   and {@link #_pop()}).

   @author Shuvra S. Bhattacharyya, Ankush Varma
   @version $Id$
*/
public class CSwitch implements JimpleValueSwitch, StmtSwitch {

    /* Construct a new CSwitch with an empty context. */
    public CSwitch() {
        super();
        _code = new Stack();
        _context = new Context();
        _targetMap = new HashMap();
        _targetCount = 0;
    }

    /** Construct a new CSwitch with a given context. */
    public CSwitch(Context context) {
        this();
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that are not associated with the             ////
    //// implemented interfaces                                      ////

    /** Register a unit as a target of a branch from some other statement.
     *  @param target the unit.
     */
    public void addTarget(Unit target) {
        _targetMap.put(target, "label" + _targetCount++);
    }

    /** Clear the set of branch targets encountered in the current method.
     */
    public void clearTargets() {
        _targetMap.clear();
        _targetCount = 0;
    }

    /** Retrieve the code generated by the switch since the last time
     *  code was retrieved from it.
     */
    public StringBuffer getCode() {
        StringBuffer result = new StringBuffer();
        Iterator fragments = _code.iterator();
        while (fragments.hasNext()) {
            result.append((StringBuffer)(fragments.next()));
        }
        _code.clear();
        return result;
    }

    /** Return the unique label associated with a unit that has been registered
     *  as the target of a branch from another statement.
     *  Uniqueness is with respect to the current method.
     *  @param unit the unit.
     *  @return the label.
     *  @exception RuntimeException If the unit has not yet been registered as
     *  the target of a branch.
     */
    public String getLabel(Unit unit) {
        String label;
        if ((label = ((String)_targetMap.get(unit))) == null) {
            throw new RuntimeException("Unit is not a branch target.\n"
                    + "The offending unit is: " + unit.toString() + "\n");
        }
        else return label;
    }

    /** Return the name of the local in the current method that represents
     *  the current class.
     *  @return the name.
     */
    public String getThisLocalName() {
        return _thisLocalName;
    }

    /** Return true if the given unit has been registered as the target of
     *  a branch from another statement.
     *  @param unit the unit.
     */
    public boolean isTarget(Unit unit) {
        return _targetMap.containsKey(unit);
    }

    /** Set the name of the local in the current method that represents
     *  the current class.
     *  @param name the name.
     */
    public void setThisLocalName(String name) {
        _thisLocalName = name;
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that implement the ConstantSwitch interface  ////

    public void caseDoubleConstant(DoubleConstant v) {
        _push("((double)" + v.toString() +")");
    }

    public void caseFloatConstant(FloatConstant v)
    {
        _push("((float)" + v.toString() +")");
    }

    /** Push the value of an integer constant onto the code stack.
     *  @param v the integer constant.
     */
    public void caseIntConstant(IntConstant v) {
        _push(v.toString());
    }

    public void caseLongConstant(LongConstant v) {
        // FIXME: verify long qualifier
        _push(v.toString());
    }

    public void caseNullConstant(NullConstant v) {
        _push("NULL");
    }

    public void caseStringConstant(StringConstant v) {
        _push(_context.newStringConstant(v.value));
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that implement the ExprSwitch interface    ////

    public void caseAddExpr(AddExpr v) {
        _generateBinaryOperation(v, "+");
    }

    public void caseAndExpr(AndExpr v) {
        //bitwise AND operator
        _generateBinaryOperation(v, "&");
    }

    public void caseCastExpr(CastExpr v)
    {
        //FIXME: Does not handle null cast
        if (!v.getOp().toString().equals("null"))
        {
            _push("("+CNames.typeNameOf(v.getCastType())+")"
                +CNames.localNameOf((Local)v.getOp()));
        }
        else
        {
            System.err.println("CSwitch.caseCastExpression does not"
                +"handle null.");

            defaultCase(v);
        }
    }

    /** Generate code for a CmpExpr expression.
     *  @param v the expression.
     */
    public void caseCmpExpr(CmpExpr v) {
        _generateCompare(v);
    }

    /** Generate code for a Cmpg expression. Presently this is equivalent
     *  to generating code for a Cmp expression since NaN is not supported
     *  at present.
     *  @param v the expression.
     */
    public void caseCmpgExpr(CmpgExpr v) {
        _generateCompare(v);
    }

    /** Generate code for a Cmpl expression. Presently this is equivalent
     *  to generating code for a Cmp expression since NaN is not supported
     *  at present.
     *  @param v the expression.
     */
    public void caseCmplExpr(CmplExpr v) {
        _generateCompare(v);
    }

    public void caseDivExpr(DivExpr v) {
        _generateBinaryOperation(v, "/");
    }

    public void caseEqExpr(EqExpr v) {
        _generateBinaryOperation(v, "==");
    }

    public void caseGeExpr(GeExpr v) {
        _generateBinaryOperation(v, ">=");
    }

    public void caseGtExpr(GtExpr v) {
        _generateBinaryOperation(v, ">");
    }

    /** Generate code for an instanceof expression.
     *  @param v the instanceof expression.
     */
    public void caseInstanceOfExpr(InstanceOfExpr v) {
        // FIXME: add support for all relevant types.
        Type type = v.getCheckType();
        if ((type instanceof RefType))
        {
            v.getOp().apply(this);
            _push(CNames.instanceOfFunction + "(" + _pop() + ", &"
                + CNames.classStructureNameOf(((RefType)type).getSootClass())
                + ")");
        }

        else
        {
            _unexpectedCase(v,
                "Only RefTypes are presently supported for 'instanceof'");
        }
    }

    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
        defaultCase(v);
    }

    public void caseLeExpr(LeExpr v) {
        _generateBinaryOperation(v, "<=");
    }

    /** Generate code for an array length expression. This
     *  is performed by inserting a call to a length computation
     *  macro from the pccg run-time library.
     *  @param v the length expression.
     */
    public void caseLengthExpr(LengthExpr v) {
        v.getOp().apply(this);
        _push(CNames.arrayLengthFunction + "(" + _pop() + ")");
    }

    public void caseLtExpr(LtExpr v) {
        _generateBinaryOperation(v, "<");
    }

    public void caseMulExpr(MulExpr v) {
        _generateBinaryOperation(v, "*");
    }

    public void caseNeExpr(NeExpr v) {
        _generateBinaryOperation(v, "!=");
    }

    public void caseNegExpr(NegExpr v)
    {
        _push("-"+CNames.localNameOf((Local)v.getOp()));
    }

    public void caseNewArrayExpr(NewArrayExpr v) {
        v.getSize().apply(this);
        String sizeCode = _pop().toString();
        _push(_generateArrayAllocation(v.getBaseType(), 1, sizeCode));
    }

    public void caseNewExpr(NewExpr v) {
        if (_debug) {
            System.out.println("new expr types: "
                    + v.getBaseType().getClass().getName()
                    + ", " + v.getType().getClass().getName() + ", "
                    + CNames.typeNameOf(v.getBaseType()) + ", "
                    + CNames.typeNameOf(v.getType()));
        }
        String name = CNames.typeNameOf(v.getType());
        _context.addIncludeFile("<stdlib.h>");
        _push("((" + name + ") malloc(sizeof(struct " + name + ")))");
    }

    public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
        if (_debug) {
            System.out.println("NewMultiArrayExpr: " + v.getSizeCount() +
            "/" + v.getSizes().size() + v.getBaseType().getClass().getName());
        }
        String sizeCode = new String();
        Iterator sizes = v.getSizes().iterator();
        while (sizes.hasNext()) {
            ((Value)sizes.next()).apply(this);
            sizeCode += _pop();
            if (sizes.hasNext()) sizeCode += ", ";
        }
        _push(_generateArrayAllocation(v.getBaseType(), v.getSizeCount(),
                sizeCode));
    }

    public void caseOrExpr(OrExpr v) {
        //bitwise OR
        _generateBinaryOperation(v,"|");
    }

    public void caseRemExpr(RemExpr v)
    {
        _generateBinaryOperation(v, "%");
    }

    public void caseShlExpr(ShlExpr v) {
        defaultCase(v);
    }

    public void caseShrExpr(ShrExpr v) {
        defaultCase(v);
    }

    /** Generate code for a special invoke expression.
     *  @param v the expression.
     *  @return the code.
     */
    public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
        // Presently, we consider one case: a call to a method
        // that is declared by the superclass of the base. This
        // occurs when the base class constructor is invoked automatically
        // from with a given class constructor.
        SootClass methodClass = v.getMethod().getDeclaringClass();
        SootMethod method = v.getMethod();
        if (!(v.getBase().getType() instanceof RefType)) {
            _unexpectedCase(v, "RefType base type expected.");
        } else {
            SootClass baseClass = ((RefType)(v.getBase().getType())).
                    getSootClass();
            if (baseClass == methodClass) {
                if (method.isStatic()) {
                    _unexpectedCase(v, "Non-static method expected.");
                }
                else {
                    _generateInstanceInvokeExpression(v);
                }
            }
            else if ((!baseClass.hasSuperclass()) ||
                    (baseClass.getSuperclass() != methodClass)) {
                _unexpectedCase(v,
                        "Expected method class to be superclass of base");
            // If we are generating code in single class mode, then
            // we are not supporting inheritance, so ignore invocation
            // of the superclass constructor.
            } else if (_context.getSingleClassMode()) {
                return;
            } else {
                v.getBase().apply(this);
                StringBuffer baseCode = _pop();
                _push(CNames.classStructureNameOf(methodClass) + ".methods." +
                        CNames.methodNameOf(method) + "((" +
                        CNames.instanceNameOf(methodClass) + ")"+ baseCode
                        + _generateArguments(v, 1) + ")");
            }
        }
    }

    public void caseStaticInvokeExpr(StaticInvokeExpr v) {
        _push(CNames.functionNameOf(v.getMethod()) + "(" +
                _generateArguments(v, 0) + ")");
    }

    public void caseSubExpr(SubExpr v) {
        _generateBinaryOperation(v, "-");
    }

    public void caseUshrExpr(UshrExpr v) {
        defaultCase(v);
    }

    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        _generateInstanceInvokeExpression(v);
    }

    public void caseXorExpr(XorExpr v)
    {
        _generateBinaryOperation(v,"^");
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that implement the JimpleValueSwitch interface ////

    /** Push the name of a local onto the code stack.
     *  @param l the local.
     */
    public void caseLocal(Local l) {
        // FIXME: do we need to sanitize the name?
        _push(CNames.localNameOf(l));
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that implement the RefSwitch interface    ////

    /** Generate code for an array reference. This is done by
     *  generating a call to a macro from the run time library
     *  with the given base (array) and index expression.
     *  @param v the array reference whose code is to be generated.
     */
    public void caseArrayRef(ArrayRef v) {
        v.getBase().apply(this);
        StringBuffer baseCode = _pop();
        v.getIndex().apply(this);
        StringBuffer indexCode = _pop();
        _push(CNames.arrayReferenceFunction + "(" + baseCode + ", "
                + indexCode + ")");
    }

    public void caseCaughtExceptionRef(CaughtExceptionRef v) {

        /*
        System.out.println("Caught exception ref of type: " +
                v.getType().toString());
        Iterator useBoxes = v.getUseBoxes().iterator();
        while (useBoxes.hasNext()) {
            Object box = useBoxes.next();
            System.out.println("use box of type " + box.getClass().getName());
        }
        defaultCase(v);
        */

        _push("exception_id");
    }

    public void caseInstanceFieldRef(InstanceFieldRef v) {
        v.getBase().apply(this);
        _push(_pop().append("->").append(CNames.fieldNameOf(v.getField())));
    }

    public void caseNextNextStmtRef(NextNextStmtRef v) {
        defaultCase(v);
    }

    public void caseParameterRef(ParameterRef v) {
        defaultCase(v);
    }

    public void caseStaticFieldRef(StaticFieldRef v) {
        SootField field = v.getField();
        _push(CNames.classStructureNameOf(field.getDeclaringClass())
                + ".classvars."
                + CNames.fieldNameOf(field));
    }

    public void caseThisRef(ThisRef v) {
        defaultCase(v);
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods that implement the StmtSwitch interface    ////

    /** Generate code for an assignment statement
     *  @param stmt the assignment statment.
     */
    public void caseAssignStmt(AssignStmt stmt) {
        stmt.getRightOp().apply(this);
        stmt.getLeftOp().apply(this);
        _push(_pop().append(" = ").append(_pop()));
    }

    public void caseBreakpointStmt(BreakpointStmt stmt) {
        defaultCase(stmt);
    }

    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
        defaultCase(stmt);
    }

    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
        defaultCase(stmt);
    }

    public void caseGotoStmt(GotoStmt stmt) {
        _push("goto " + getLabel(stmt.getTarget()));
    }

    public void caseIdentityStmt(IdentityStmt stmt) {
        Value rightOp = stmt.getRightOp();
        if ((rightOp instanceof ParameterRef) || (rightOp instanceof ThisRef))
            return;
        else {
            rightOp.apply(this);
            stmt.getLeftOp().apply(this);
            _push(_pop().append(" = ").append(_pop()));
        }
    }

    /** Generate code for an if statement.
     *  @param stmt the if statement.
     */
    public void caseIfStmt(IfStmt stmt) {
        stmt.getCondition().apply(this);
        _push(new StringBuffer("if (" + _pop() + ") "
                + "goto " + getLabel(stmt.getTarget())));
    }

    public void caseInvokeStmt(InvokeStmt stmt) {
        stmt.getInvokeExpr().apply(this);
    }

    public void caseLookupSwitchStmt(LookupSwitchStmt stmt)
    {
        defaultCase(stmt);
    }

    public void caseNopStmt(NopStmt stmt)
    {
        //do nothing
        //defaultCase(stmt);
    }

    public void caseRetStmt(RetStmt stmt) {
        defaultCase(stmt);
    }

    public void caseReturnStmt(ReturnStmt stmt) {
        stmt.getOp().apply(this);
        _push("memcpy(env, caller_env, sizeof(jmp_buf));\n"
            +"        epc = caller_epc;\n"
            +"        return " + _pop());
    }

    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
        _push("memcpy(env, caller_env, sizeof(jmp_buf));\n");
        _push("        epc = caller_epc;\n");
        _push("        return");
    }

    public void caseTableSwitchStmt(TableSwitchStmt stmt) {
        defaultCase(stmt);
    }

    public void caseThrowStmt(ThrowStmt stmt) {

        _push("exception_id = "+CNames.localNameOf((Local)stmt.getOp())+";\n");
        _push("        longjmp(env, epc)");

    }

    ///////////////////////////////////////////////////////////////////
    //// public methods for the default case                       ////

    public void defaultCase(Object obj) {
        if (obj instanceof Stmt)
        {
            _push("/*UNHANDLED STATEMENT: " + obj.getClass().getName() + "*/");
        }
        else if (obj instanceof Expr)
        {
            _push("0 /*UNHANDLED EXPRESSION HERE:"
                +obj.getClass().getName() +"*/");
        }
        else //neither statement nor expression
        {
            _push("< UNHANDLED: "+obj.getClass().getName()+">");
        }

        System.err.println("Unsupported visitation type: "
                + obj.getClass().getName() + "(ignored).");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for a list of arguments from an invoke expression.
     *  @param expression the invoke expression.
     *  @param previousArguments the number of arguments that have already
     *  been generated for this invoke expression.
     *  @return the code.
     */
    protected String _generateArguments(InvokeExpr expression,
            int previousArguments) {
        StringBuffer code = new StringBuffer();
        Iterator args = expression.getArgs().iterator();
        int count = previousArguments;
        while (args.hasNext()) {
            if (count++ > 0) code.append(", ");
            ((Value)(args.next())).apply(this);
            code.append(_pop());
        }
        return code.toString();
    }

    protected String _generateArrayAllocation(Type elementType,
            int dimensionsToFill, String sizeCode) {
        if (!((elementType instanceof BaseType) ||
                (elementType instanceof ArrayType))) {
            _unexpectedCase(elementType, "unsupported array element type");
            return "";
        } else {
            // Determine the name of the run-time variable that
            // represents the array element class.
            String elementClass;
            String elementSizeType = "void*";
            int emptyDimensions = 0;
            if (elementType instanceof RefType) {
                elementClass = CNames.typeNameOf(elementType);
            }
            else if (elementType instanceof ArrayType) {
                BaseType baseType = ((ArrayType)elementType).baseType;
                elementClass = CNames.typeNameOf(baseType);
                emptyDimensions = ((ArrayType)elementType).numDimensions;
            } else {
                elementClass = CNames.arrayClassPrefix +
                        CNames.typeNameOf(elementType) + "_elem";
                elementSizeType = CNames.typeNameOf(elementType);
            }

            // Generate code for a call to a run-time function that
            // will allocate an array. This code should be completed
            // by the calling method with the appropriate dimension
            // and size.
            return CNames.arrayAllocateFunction + "(" +
                    elementClass + ", sizeof(" + elementSizeType + "), " +
                    dimensionsToFill + ", " + emptyDimensions + ", " +
                    sizeCode + ")";
        }
    }

    /** Generate code for a binary operation expression.
     *  @param expression the expression.
     *  @param operator the string representation of the binary operator.
     */
    protected void _generateBinaryOperation(BinopExpr expression,
            String operator) {
        expression.getOp2().apply(this);
        expression.getOp1().apply(this);
        _push(_pop().append(" " + operator + " ").append(_pop()));
    }

    /** Generate code for a compare expression.
     * The following semantics of a compare expression is assumed: <BR>
     * (op1 > op2)  ==> return 1 <BR>
     * (op1 < op2)  ==> return -1 <BR>
     * (op1 == op2) ==> return 0 <BR>
     *  @param v the expression.
     */
    protected void _generateCompare(BinopExpr v) {
        v.getOp2().apply(this);
        v.getOp1().apply(this);
        String op1 = _pop().toString();
        String op2 = _pop().toString();
        _push("((" + op1 + " > " + op2 + ") ?  1 : (" +
                "(" + op1 + " < " + op2 + ") ?  -1 : 0))");

    }

    /** Generate code for an instance invoke expression.
     *  @param expression the instance invoke expression.
     */
    protected void _generateInstanceInvokeExpression(
            InstanceInvokeExpr expression) {
        expression.getBase().apply(this);
        StringBuffer instanceName = _pop();
        StringBuffer code = new StringBuffer(instanceName + "->class->methods."
                + CNames.methodNameOf(expression.getMethod()));
        code.append("(" + instanceName + _generateArguments(expression, 1)
                + ")");
        _push(code);
    }

    /** Retrieve and remove the code at the top of the code stack.
     *  Code that exists on entry to a visitation method should not
     *  be removed (popped) by the method.
     *  @return the code at the top of the code stack.
     */
    protected StringBuffer _pop() {
        return (StringBuffer)(_code.pop());
    }

    /** Push a string buffer onto the code stack. On exit from a visitation
     *  method, any code that results from the method should be
     *  placed (pushed) on the stack in a single string buffer (or
     *  through a single call to {@link #_push(String)}). Any other code
     *  (intermediate strings that do not represent code generated by the
     *  method) should be removed from the stack before exiting the
     *  visitation method.
     *  @param codeString the string buffer.
     */
    protected void _push(StringBuffer codeString) {
        _code.push(codeString);
    }

    /** Push a string onto the code stack. See {@link #_push(StringBuffer)}.
     *  @param codeString the string.
     */
    protected void _push(String codeString) {
        _code.push(new StringBuffer(codeString));
    }

    /** Report an error, with an associated object and a descriptive message,
     *  for an unexpected code generation situation. The resulting message
     *  is pushed onto the top of the code stack.
     *  @param object the associated object.
     *  @param message the descriptive message.
     */
    protected void _unexpectedCase(Object object, String message) {
        _push("<" + object.getClass().getName() + ">");
        System.err.println("Unexpected code conversion case in CSwitch:\n"
                + "        " + message + "\n        Case object is of class "
                + object.getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A stack of StringBuffers corresponding to code that has been generated
    // by this switch. The most recently-generated code is at the top of
    // the stack, and in general, older code resides lower in the stack.
    private Stack _code;

    // Code generation context.
    private Context _context;

    // Control local debugging output
    private static boolean _debug = false;

    // The number of branch targets encountered so far in the current method.
    private int _targetCount;

    // Map from units in a method into labels (for goto statements in the
    // generated code).
    private HashMap _targetMap;

    // The name of the local in the current method that represents the current
    // class.
    // FIXME: do we really need this field, and the associated methods?
    private String _thisLocalName;
}
