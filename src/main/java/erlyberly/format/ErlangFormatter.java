package erlyberly.format;

import java.util.ArrayList;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import erlyberly.node.OtpUtil;

public class ErlangFormatter implements TermFormatter {

    @Override
    public String toString(OtpErlangObject obj) {
        return appendToString(obj, new StringBuilder()).toString();
    }
    
    @Override
    public StringBuilder appendToString(OtpErlangObject obj, StringBuilder sb) {
        if(obj instanceof OtpErlangBinary) {
            sb.append(binaryToString((OtpErlangBinary) obj));
        }
        else if(obj instanceof OtpErlangPid) {
            sb.append(pidToString((OtpErlangPid) obj));
        }
        else if(OtpUtil.isErlyberlyRecord(obj)) {
            OtpErlangTuple record = (OtpErlangTuple) obj;
            OtpErlangAtom recordName = (OtpErlangAtom) record.elementAt(1);
            OtpErlangList fields = (OtpErlangList) record.elementAt(2);
            sb.append("{").append(recordName).append(", ");
            for(int i=0; i < fields.arity(); i++) {
                if(i != 0) {
                    sb.append(", ");
                }
                appendToString(fields.elementAt(i), sb);
            }
            sb.append("}");
        }
        else if(OtpUtil.isErlyberlyRecordField(obj)) {
            OtpErlangObject fieldObj = ((OtpErlangTuple)obj).elementAt(2);
            appendToString(fieldObj, sb);
        }
        else if(obj instanceof OtpErlangTuple || obj instanceof OtpErlangList) {
            String brackets = bracketsForTerm(obj);
            OtpErlangObject[] elements = OtpUtil.elementsForTerm(obj);
            
            sb.append(brackets.charAt(0));
            
            for(int i=0; i < elements.length; i++) {
                if(i != 0) {
                    sb.append(", ");
                }
                appendToString(elements[i], sb);
            }
            sb.append(brackets.charAt(1));
        }
        else if(obj instanceof OtpErlangString) {
            sb.append(obj.toString().replace("\n", "\\n"));
        }
        else {
            sb.append(obj.toString());
        }
        return sb;
    }
    
    public static String pidToString(OtpErlangPid pid) {
        return "<0." + pid.id() + "." + pid.serial() + ">";
    }
    
    public String binaryToString(OtpErlangBinary bin) {
        StringBuilder s = new StringBuilder("<<");
        
        boolean inString = false;
        
        for (int b : bin.binaryValue()) {
            if(b > 31 && b < 127) {
                if(!inString) {
                    if(s.length() > 2) {
                        s.append(", ");
                    }
                    
                    s.append("\"");
                }
                inString = true;
                s.append((char)b);
            }
            else {
                if(inString) {
                    s.append("\"");
                    inString = false;
                }
                
                if(s.length() > 2) {
                    s.append(", ");
                }

                if(b < 0) {
                    b = 256 + b;
                }
                s.append(Integer.toString(b));
            }
        }
        
        if(inString) {
            s.append("\"");
        }
        
        s.append(">>");
        
        return s.toString();
    }


    public String bracketsForTerm(OtpErlangObject obj) {
        assert obj != null;
        
        if(obj instanceof OtpErlangTuple)
            return "{}";
        else if(obj instanceof OtpErlangList)
            return "[]";
        else
            throw new RuntimeException("No brackets for type " + obj.getClass());
    }

    /**
     * Convert an MFA tuple to a string, where the MFA must have the type:
     *
     * {Module::atom(), Function::atom(), Args::[any()]}.
     */
    @Override
    public String mfaToString(OtpErlangTuple mfa) {
        StringBuilder sb = new StringBuilder();
        sb.append(mfa.elementAt(0))
          .append(":")
          .append(mfa.elementAt(1))
          .append("(");
        OtpErlangList args = (OtpErlangList) mfa.elementAt(2);
        ArrayList<String> stringArgs = new ArrayList<>();
        for (OtpErlangObject arg : args) {
            stringArgs.add(toString(arg));
        }
        sb.append(String.join(", ", stringArgs));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String modFuncArityToString(OtpErlangTuple mfa) {
        StringBuilder sb = new StringBuilder();
        OtpErlangList argsList = (OtpErlangList) mfa.elementAt(2);
        sb.append(mfa.elementAt(0))
          .append(":")
          .append(mfa.elementAt(1))
          .append("/").append(argsList.arity());
        return sb.toString();
    }

    @Override
    public String exceptionToString(OtpErlangAtom errorClass, OtpErlangObject errorReason) {
        return errorClass + ":" +  toString(errorReason);
    }

    @Override
    public String emptyTupleString() {
        return "{ }";
    }

    @Override
    public String tupleLeftParen() {
        return "{";
    }

    @Override
    public String tupleRightParen() {
        return "}";
    }

    @Override
    public String emptyListString() {
        return "[ ]";
    }

    @Override
    public String listLeftParen() {
        return "[";
    }

    @Override
    public String listRightParen() {
        return "]";
    }
}
