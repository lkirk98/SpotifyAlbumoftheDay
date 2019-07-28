package com.project.logan.spotifyalbumoftheday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class SigninActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1010;
    private static final String REDIRECT = "albumoftheday://result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
    }

    /* Runs when the user clicks the signin button */
    public void onClickSignin(View v){
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder("aaebb2a5dc4149eb88e306de619ea186",
                                                    AuthenticationResponse.Type.TOKEN,
                                                    REDIRECT);

        builder.setScopes(new String[]{"playlist-modify-public",
                                        "user-library-read"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    /* Runs after user tries to authenticate through the spotify login activity */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                /* on successful authentication, open the main activity */
                case TOKEN:
                    Intent create_main_activity = new Intent(this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("access_token", response.getAccessToken());
                    create_main_activity.putExtras(bundle);
                    startActivity(create_main_activity);
                    break;

                // Auth flow returned an error
                case ERROR:
                    System.out.println(response.getError());
                default:
                    Toast.makeText(this,
                            "Something went wrong. Try again or lick the nuts.",
                            Toast.LENGTH_LONG).show();
            }
        }
    }
}
