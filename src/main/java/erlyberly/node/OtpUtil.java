package erlyberly.node;

import java.util.HashMap;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

/**
 * Sin bin for utils dealing with jinterface.
 */
public class OtpUtil {

	private static final OtpErlangAtom ERROR_ATOM = atom("error");
	public static final OtpErlangAtom OK_ATOM = atom("ok");
	
	
	public static OtpErlangTuple tuple(OtpErlangObject... obj) {
		return new OtpErlangTuple(obj);
	}


	public static OtpErlangAtom atom(String name) {
		return new OtpErlangAtom(name);
	}

	/**
	 * Take an {@link OtpErlangList} of erlang key value tuples and converts it to a map.
	 */
	public static HashMap<Object, Object> propsToMap(OtpErlangList pinfo) {
		HashMap<Object, Object> map = new HashMap<>();
		for (OtpErlangObject otpErlangObject : pinfo) {
			if(otpErlangObject instanceof OtpErlangTuple && ((OtpErlangTuple) otpErlangObject).arity() == 2) {
				OtpErlangTuple tuple = ((OtpErlangTuple) otpErlangObject);
				map.put(tuple.elementAt(0), tuple.elementAt(1));
			}
		}
		return map;
	}
	
	public static String otpObjectToString(OtpErlangObject obj) {
		if(obj instanceof OtpErlangBinary)
			return binaryToString((OtpErlangBinary) obj);
		else if(obj instanceof OtpErlangPid) {
			return pidToString((OtpErlangPid) obj);
		}
		else
			return obj.toString();
	}
	
	public static String pidToString(OtpErlangPid pid) {
		return "<0." + pid.id() + "." + pid.serial() + ">";
	}
	
	public static String binaryToString(OtpErlangBinary bin) {
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

	public static boolean isTupleTagged(OtpErlangObject tag, OtpErlangObject result) {
		return isTupleTagged(tag, 0, result);
	}

	public static boolean isTupleTagged(OtpErlangObject tag, int index, OtpErlangObject result) {
		boolean r = false;
		
		if(result instanceof OtpErlangTuple) {
			OtpErlangTuple resultTuple = (OtpErlangTuple) result;
			r = resultTuple.arity() > 0 && resultTuple.elementAt(0).equals(tag);
		}
		
		return r;
	}
	
	public static boolean isErrorReason(OtpErlangObject reason, OtpErlangObject error) {
		assert isTupleTagged(ERROR_ATOM, error) : "tuple " + error + "is not tagged with 'error'";
		return isTupleTagged(reason, 1, error);
	}
	


	public static OtpErlangList toOtpList(OtpErlangObject obj) {
		if(obj instanceof OtpErlangList) {
			return (OtpErlangList) obj;
		}
		else if(obj instanceof OtpErlangString) {
			OtpErlangString s = (OtpErlangString) obj;
			
			return new OtpErlangList(s.stringValue());
		}
		else {
			throw new ClassCastException("" + obj + " cannot be converted to an OtpErlangList");
		}
	}	
}
