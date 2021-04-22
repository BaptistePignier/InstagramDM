package com.pignier.instagramdm.Utils;

import android.media.AudioAttributes.Builder;
import android.media.AudioAttributes; 
import android.media.MediaPlayer;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

public class AudioHelper{
	int audio_played_position;
	
	public void set_audio_played_position(int new_position){
		this.audio_played_position = new_position;
	}
	public int get_audio_played_position(){
		return audio_played_position;
	}
	
	public MediaPlayer createPlayer(AudioAttributes attributes){
		MediaPlayer player = new MediaPlayer();
		player.setAudioAttributes(attributes);
		return player;
	}
	public AudioAttributes createAttributes(){
		AudioAttributes attributes = new AudioAttributes.Builder()
			.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			.build();
		return attributes;
	}
	public AudioFocusRequest createFocusRequest(AudioAttributes attributes){
		AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
		 .setAudioAttributes(attributes)
		 .build();
		return focusRequest;
	}
}