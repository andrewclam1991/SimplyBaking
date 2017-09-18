package com.andrewclam.bakingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrewclam.bakingapp.models.Step;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.View.GONE;
import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;

/**
 * A fragment representing a single Step detail screen.
 * This fragment is either contained in a {@link StepListActivity}
 * in two-pane mode (on tablets) or a {@link StepDetailActivity}
 * on handsets.
 */
public class StepDetailFragment extends Fragment implements Target, Player.EventListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_RECIPE_STEP = "item_id";
    /**
     * The fragment argument representing the boolean flag on whether this fragment is loaded
     * as part of a two-pane layout
     */
    public static final String ARG_TWO_PANE_MODE = "two_pane_mode";

    /**
     * Debug TAG
     */
    private static final String TAG = StepDetailFragment.class.getSimpleName();
    /**
     * The Step content this fragment is presenting.
     */
    private Step mItem;

    /**
     * The boolean flag to keep track whether the fragment is displayed in two pane mode
     */
    private boolean mTwoPane;

    /**
     * Context - for getting resources
     */
    private Context mContext;

    /**
     * Step Title
     */
    private String mTitle;

    /**
     * SavedInstanceState Key
     */
    private final static String EXTRA_STEP_TITLE = "extra_step_title";
    private final static String EXTRA_TWO_PANE = "extra_two_pane";

    /**
     * Interface Callback listener (parent activity) to change title
     */
    private OnStepDetailFragmentInteraction mListener;

    /**
     * TODO [ExoPlayer Media Playback ] Step 1 - Declare the Player required vars
     * MediaSession, ExoPlayer and its UI Implementation
     * With notificationManager to show User's progress
     */
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mExoPlayerView;
    private PlaybackStateCompat.Builder mStateBuilder;
    /**
     * MediaStyle Notification
     */
    private NotificationManager mNotificationManager;
    private static MediaSessionCompat mMediaSession;
    private static final int NOTIFICATION_PENDING_INTENT_RC = 2333;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = PACKAGE_NAME + ".media_notification";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = PACKAGE_NAME +
            " media style notification";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StepDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the context
        mContext = getContext();

        if (getArguments().containsKey(ARG_RECIPE_STEP)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = Parcels.unwrap(getArguments().getParcelable(ARG_RECIPE_STEP));
            mTwoPane = getArguments().getBoolean(ARG_TWO_PANE_MODE);
            assert mItem != null;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.step_detail, container, false);

        // Setup the activity bar title (show step and a short description)
        setupTitle(savedInstanceState);

        // Reference View and run assertion checks before proceeding
        TextView stepDescriptionTv = rootView.findViewById(R.id.step_detail);
        ImageView stepThumbnailIv = rootView.findViewById(R.id.step_thumbnail);
        mExoPlayerView = rootView.findViewById(R.id.step_video_player_view);

        assert stepDescriptionTv != null;
        assert stepThumbnailIv != null;
        assert mExoPlayerView != null;

        /* Bind Data (Video, Image Thumbnail and Description) */
        // Bind Step Video
        // Check if the recipe step has a video (may be null or empty)
        String videoURL = mItem.getVideoURL();
        if (videoURL != null && !videoURL.isEmpty()) {
            setupExoPlayerView();
            setupMediaSession();
            setupExoPlayer(Uri.parse(videoURL));

            // Has Video (Video Full Screen Mode)
            // If the device is in landscape mode, let video take full screen mode on Phones
            // (if it isn't also in two pane mode, landscape in tablets)
            if (rootView.findViewById(R.id.step_detail_container_land) != null && !mTwoPane) {
                rootView.findViewById(R.id.step_detail_content_sv).setVisibility(View.GONE);
                stepDescriptionTv.setVisibility(View.GONE);
                stepThumbnailIv.setVisibility(View.GONE);
            }
        } else {
            // No video for this particular step, hide the player view
            mExoPlayerView.setVisibility(GONE);
        }


        // Bind Step Image Thumbnail (may be null or empty)
        // Check if there is a thumbnail image in the step
        String thumbnailURL = mItem.getThumbnailURL();
        if (thumbnailURL != null && !thumbnailURL.isEmpty())
        {
            Picasso.with(mContext).load(Uri.parse(thumbnailURL)).into(stepThumbnailIv);
        }else
        {
            // Make the image view gone
            stepThumbnailIv.setVisibility(GONE);
        }

        // Bind Step Description
        // Show the step description as text in a TextView.
        stepDescriptionTv.setText(mItem.getDescription());

        return rootView;
    }

    /**
     * subroutine method to set the app bar title in the activity level
     * using the listener callback and savedInstanceState to handle device rotation
     *
     * @param savedInstanceState the activity/fragment's savedInstanceState
     */
    private void setupTitle(Bundle savedInstanceState) {
        if (savedInstanceState != null)
        {
            mTitle = savedInstanceState.getString(EXTRA_STEP_TITLE);
            mTwoPane = savedInstanceState.getBoolean(EXTRA_TWO_PANE);
        }else
        {
            // No saved instance state, form the title using the item
            mTitle = getString(R.string.step, mItem.getId()) + " "
                    + mItem.getShortDescription();
        }

        // Call activities to set the title of the app bar
        // two pane mode doesn't need a step-by-step title change
        if (!mTwoPane) {
            mListener.setTitle(mTitle);
        }
    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 2 - Setup the ExoPlayView
     * and bind the exoPlayer instance.
     * <p>
     * setupExoPlayerView()
     */
    private void setupExoPlayerView() {
        // SetDefaultArtwork()
        // Get the thumbnailUrl of the step, if available,
        // load it with Picasso and then set it as the player
        // view's default artWork on bitMap loaded
        String thumbnailURLStr = mItem.getThumbnailURL();
        if (thumbnailURLStr != null && !thumbnailURLStr.isEmpty()) {
            // mExoPlayerView.setDefaultArtwork(bitmap) is executed
            // when the Picasso task returns with a result on the image
            // loading, this will be done asynchronously
            Picasso.with(mContext).load(thumbnailURLStr).into(this);
        }

        /* PlayView Control Customizations */
        mExoPlayerView.setControllerAutoShow(false);
    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 3 - Setup the MediaSession
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void setupMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(mContext, TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 4 - Setup the ExoPlayer with step video Uri
     * and bind the exoPlayer instance.
     * <p>
     * setupExoPlayerView()
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void setupExoPlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);

            // Set player to the player UI view
            mExoPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this fragment.
            mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(mContext, PACKAGE_NAME);
            MediaSource mediaSource = new ExtractorMediaSource(
                    mediaUri,
                    new DefaultDataSourceFactory(mContext, userAgent),
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 5 - Show media style notification
     * for user to return to an active recipe step playback
     * <p>
     * Shows Media Style notification, with actions that depend on the current MediaSession
     * PlaybackState.
     *
     * @param state The PlaybackState of the MediaSession.
     */
    private void showNotification(PlaybackStateCompat state) {
        mNotificationManager = (NotificationManager)
                mContext.getSystemService(NOTIFICATION_SERVICE);

        assert mNotificationManager != null;

        /* Implement NotificationChannel for Devices running Android O or later*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_DESCRIPTION,
                    NotificationManager.IMPORTANCE_LOW
            );

            // Configure the notification channel.
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,
                NOTIFICATION_CHANNEL_ID);

        // Set the NotificationAction resources
        int icon;
        String play_pause;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = getString(R.string.play);
        }

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new NotificationCompat
                .Action(R.drawable.exo_controls_previous, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (mContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        Intent intent = new Intent(mContext, StepDetailActivity.class);
        intent.putExtra(ARG_RECIPE_STEP, Parcels.wrap(mItem));
        intent.putExtra(ARG_TWO_PANE_MODE,mTwoPane);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                mContext,
                NOTIFICATION_PENDING_INTENT_RC,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_cupcake)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setOnlyAlertOnce(true) // No subsequent sound or vibration if it exists
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * A method to Release ExoPlayer resources when
     * the fragment no longer needs it.
     */
    private void releasePlayer() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }

        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    /**
     * Callback from Picasso tasking loading the thumbnail URL
     *
     * @param bitmap the bitmap loaded by the Picasso
     * @param from   the origin of the bitmap
     */
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        // Got bitmap from Uri with Picasso loader
        mExoPlayerView.setDefaultArtwork(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    /**
     * ExoPlayer Event Listeners
     */
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 5 - Handles when player changes states
     * by content ready, user pausing, or simply exiting the app. Syncs the player's states
     * with the MediaSession
     * <p>
     * Method that is called when the ExoPlayer state changes. Used to update the MediaSession
     * PlayBackState to keep in sync, and post the media notification.
     *
     * @param playWhenReady true if ExoPlayer is playing, false if it's paused.
     * @param playbackState int describing the state of ExoPlayer. Can be STATE_READY, STATE_IDLE,
     *                      STATE_BUFFERING, or STATE_ENDED.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == Player.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == Player.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification(mStateBuilder.build());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }



    /**
     * Fragment Life Cycle Callbacks and SavedInstance
     */


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStepDetailFragmentInteraction)
        {
            mListener = (OnStepDetailFragmentInteraction) context;
        }else
        {
            throw new RuntimeException(
                    context.getClass().getSimpleName()
                    + " must implement " +
                    OnStepDetailFragmentInteraction.class.getSimpleName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mContext != null) {
            // Cleanup picasso task, no longer need to
            // fetch the thumbnail if it is in progress
            Picasso.with(mContext).cancelRequest(this);
        }

        // Pause the video playback
        if (mExoPlayer != null) mExoPlayer.setPlayWhenReady(false);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_STEP_TITLE, mTitle);
        outState.putBoolean(EXTRA_TWO_PANE,mTwoPane);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release the player when the fragment is destroyed, and
        // mark media session non=active
        releasePlayer();
        if (mMediaSession != null) mMediaSession.setActive(false);

    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 6 - Handles external client controls via Receiver
     * setup a broadcast receiver and handle mediaSession callbacks onReceive
     * <p>
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    /**
     * TODO [ExoPlayer Media Playback ] Step 7 - Handles MediaSession callbacks
     * ex, headphone buttons, car bluetooth control, notification with the MediaSession
     * <p>
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    /**
     * Interface
     */
    public interface OnStepDetailFragmentInteraction
    {
        void setTitle(String title);
    }
}
