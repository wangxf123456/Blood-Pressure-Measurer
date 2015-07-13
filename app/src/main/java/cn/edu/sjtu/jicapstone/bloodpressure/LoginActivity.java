package cn.edu.sjtu.jicapstone.bloodpressure;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.FindCallback;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Date;
import java.util.List;
import java.lang.String;

public class LoginActivity extends Activity {

    static String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signInButton = (Button)findViewById(R.id.signInButton);
        Button signUpButton = (Button)findViewById(R.id.signUpButton);
        final EditText userNameText = (EditText)findViewById(R.id.nameText);
        final EditText passwordText = (EditText)findViewById(R.id.passwordText);

        signInButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                final String username;
                final String password;
                username = userNameText.getText().toString();
                password = passwordText.getText().toString();

                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", username);
                query.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> objects, ParseException e) {
                        if (e == null && objects.size() == 0) {

                        } else {
                            Toast.makeText(LoginActivity.this, "This user name already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String username;
                final String password;
                username = userNameText.getText().toString();
                password = passwordText.getText().toString();

                if (username.length() == 0 || password.length() == 0) {
                    Toast.makeText(LoginActivity.this, "invalid user name or password", Toast.LENGTH_SHORT).show();
                }

                final ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password);

                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    currentUser.logOut();
                }

                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", username);
                query.findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> objects, ParseException e) {
                        if (e == null && objects.size() == 0) {
                            user.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.i(TAG, "sign up done");
                                        Intent nextScreen = new Intent(getApplicationContext(), MainActivity.class);
                                        nextScreen.putExtra("username", username);
                                        nextScreen.putExtra("userid", user.getObjectId());
                                        startActivity(nextScreen);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Sign up error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "This user name already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
