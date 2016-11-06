package ch.msengineering.budgetr;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cengiz on 31.10.16.
 * the first registration class which allows to define name, email and others...
 */

// allows us to make a request to the db-server register.php file
public class RegisterRequest extends StringRequest {

    private static final String REGISTER_REQUEST_URL = "http://whitehat.ch/Register.php";
    private Map<String, String> params;

    //first instance which is run when the first instance is created
    public RegisterRequest(String name, String email, String username, String passport, Response.Listener<String> listener) {
        super(Method.POST,REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("username", username);
        params.put("password", passport);
    }

    //get params and enter
    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
