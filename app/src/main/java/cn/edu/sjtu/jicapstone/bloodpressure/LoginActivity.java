package cn.edu.sjtu.jicapstone.bloodpressure;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.FindCallback;

import java.util.Date;
import java.util.List;

public class LoginActivity extends Activity {

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

                ParseQuery<ParseObject> query = ParseQuery.getQuery("BloodPressureUser");
                query.whereEqualTo("userName", username);
                query.whereEqualTo("password", password);

                // validate user
                System.out.printf("%s, %s", username, password);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> userList, ParseException e) {
                        if (e == null && userList.size() > 0) {
                            System.out.println("Log in successful as " + username);
                            Intent nextScreen = new Intent(getApplicationContext(), MainActivity.class);
                            nextScreen.putExtra("username", username);
//                            nextScreen.putExtra("password", password);
                            startActivity(nextScreen);
                        } else {
                            System.out.println("Log in failed");
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

                ParseQuery<ParseObject> query = ParseQuery.getQuery("BloodPressureUser");
                query.whereEqualTo("userName", username);

                // validate user
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> userList, ParseException e) {
                        if (e == null && userList.size() > 0) {

                        } else {
                            ParseObject newUser = new ParseObject("BloodPressureUser");
                            newUser.put("userName", username);
                            newUser.put("password", password);
                            newUser.saveInBackground();

                            System.out.println("saved " + username + " " + password);
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
