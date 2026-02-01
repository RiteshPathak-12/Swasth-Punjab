package com.example.swasthpunjab;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No layout needed, launching Jitsi directly

        String videoUrl = getIntent().getStringExtra("VIDEO_URL");
        String displayName = getIntent().getStringExtra("DISPLAY_NAME"); // Pass Name

        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid Meeting Link", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        launchJitsiMeeting(videoUrl, displayName);
    }

    private void launchJitsiMeeting(String fullUrl, String name) {
        try {
            // Reverting to meet.jit.si for better compatibility with SDK
            // URL Format: https://meet.jit.si/RoomName
            
            URL url = new URL(fullUrl);
            String roomName = url.getPath().replace("/", ""); 

            // Set User Info
            JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
            if (name != null) userInfo.setDisplayName(name);

            // Build Options
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(roomName)
                    .setUserInfo(userInfo)
                    .setFeatureFlag("invite.enabled", false) 
                    .setFeatureFlag("meeting-password.enabled", false)
                    .setFeatureFlag("pip.enabled", true)
                    .setAudioMuted(false)
                    .setVideoMuted(false)
                    .build();

            // Launch Native Jitsi Activity
            JitsiMeetActivity.launch(this, options);
            finish();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
