package ch.msengineering.budgetr;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cengiz on 31.10.16.
 * the register request class which sends the json to the server as a post request
 */

// allows to make a request to the db-server register.php file which is on the server
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
