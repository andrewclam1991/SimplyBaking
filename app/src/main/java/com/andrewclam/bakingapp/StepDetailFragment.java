/*
 * Copyright (c) 2017 Andrew Chi Heng Lam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andrewclam.bakingapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewclam.bakingapp.models.Step;
import com.andrewclam.bakingapp.utils.NotificationUtil;
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
import com.google.android.exoplayer2.ui.BuildConfig;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import static android.view.View.GONE;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_ID;
import static com.andrewclam.bakingapp.Constants.EXTRA_RECIPE_NAME;
import static com.andrewclam.bakingapp.Constants.EXTRA_STEP_POSITION;
import static com.andrewclam.bakingapp.Constants.PACKAGE_NAME;

/**
 * A fragment representing a single Step detail screen.
 * This fragment is either contained in a {@link RecipeDetailActivity}
 * in two-pane mode (on tablets) or a {@link StepDetailActivity}
 * on handsets.
 */
public class StepDetailFragment extends Fragment implements Target, Player.EventListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String EXTRA_RECIPE_STEP = "item_id";
    /**
     * The fragment argument representing the boolean flag on whether this fragment is loaded
     * as part of a two-pane layout
     */
    public static final String EXTRA_TWO_PANE_MODE = "two_pane_mode";

    /**
     * Debug and fragment TAG
     */
    public static final String TAG = StepDetailFragment.class.getSimpleName();
    /**
     * Context - for getting resources
     */
    private Context mContext;
    /**
     * The Step content this fragment is presenting.
     */
    private Step mStepItem;
    /**
     * The boolean flag to keep track whether the fragment is displayed in two pane mode
     */
    private boolean mTwoPane;
    /**
     * Step Title
     */
    private String mCurrentStepTitle;
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
    private static MediaSessionCompat mMediaSession;
    private ProgressBar mVideoLoadingPb;
    /**
     * For MediaStyle Notification
     * (PendingIntent to host activities
     * need the recipeId that this step is part of)
     */
    private long mRecipeId;
    private String mRecipeName;
    private int mStepPosition;
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_PENDING_INTENT_RC = 2333;

    /**
     * Thumbnail bitmap of the step (if available)
     */
    private Bitmap mRecipeThumbnailIcon;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StepDetailFragment() {
    }

    /**
     * Factory static method to create new instance of the fragment
     * with the required parameters
     *
     * @param mStepItem the Step item, containing the recipe step's specific id
     * @param mTwoPane  to indicate whether the fragment is part of the twoPane layout
     * @return a new instance of the fragment
     */
    public static StepDetailFragment newInstance(Long mRecipeId,
                                                 String mRecipeName,
                                                 int mStepPosition,
                                                 Step mStepItem,
                                                 boolean mTwoPane) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_RECIPE_ID, mRecipeId);
        args.putString(EXTRA_RECIPE_NAME,mRecipeName);
        args.putInt(EXTRA_STEP_POSITION, mStepPosition);
        args.putParcelable(EXTRA_RECIPE_STEP, Parcels.wrap(mStepItem));
        args.putBoolean(EXTRA_TWO_PANE_MODE, mTwoPane);

        StepDetailFragment fragment = new StepDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the context
        mContext = getContext();

        if (getArguments().containsKey(EXTRA_RECIPE_ID)
                && getArguments().containsKey(EXTRA_RECIPE_NAME)
                && getArguments().containsKey(EXTRA_STEP_POSITION)
                && getArguments().containsKey(EXTRA_RECIPE_STEP)
                && getArguments().containsKey(EXTRA_TWO_PANE_MODE)) {

            mRecipeId = getArguments().getLong(EXTRA_RECIPE_ID);
            mRecipeName = getArguments().getString(EXTRA_RECIPE_NAME);
            mStepPosition = getArguments().getInt(EXTRA_STEP_POSITION);
            mStepItem = Parcels.unwrap(getArguments().getParcelable(EXTRA_RECIPE_STEP));
            mTwoPane = getArguments().getBoolean(EXTRA_TWO_PANE_MODE);

            if (BuildConfig.DEBUG) {
                if (mStepItem == null)
                    throw new AssertionError("fragment argument doesn't contain the step to show");
            }

        } else {
            String errorMsg = TAG + " instance created without the required arguments";
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg);
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

        if (BuildConfig.DEBUG) {
            if (stepDescriptionTv == null)
                throw new AssertionError("can't find the stepDescriptionTv in the layout");

            if (stepThumbnailIv == null)
                throw new AssertionError("can't find the stepThumbnailTv in the layout");

            if (mExoPlayerView == null)
                throw new AssertionError("can't find the mExoPlayerView in the layout");
        }

        /* Bind Data (Video, Image Thumbnail and Description) */
        // Bind Step Video
        // Check if the recipe step has a video (may be null or empty)
        String videoURL = mStepItem.getVideoURL();
        if (videoURL != null && !videoURL.isEmpty()) {
            // Reference the video loading progress bar
            mVideoLoadingPb = rootView.findViewById(R.id.video_loading_pb);

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
        String thumbnailURL = mStepItem.getThumbnailURL();
        if (thumbnailURL != null && !thumbnailURL.isEmpty()) {
            Picasso.with(mContext).load(Uri.parse(thumbnailURL)).into(stepThumbnailIv);
        } else {
            // Make the image view gone
            stepThumbnailIv.setVisibility(GONE);
        }

        // Bind Step Description
        // Show the step description as text in a TextView.
        stepDescriptionTv.setText(mStepItem.getDescription());

        return rootView;
    }

    /**
     * subroutine method to set the app bar title in the activity level
     * using the listener callback and savedInstanceState to handle device rotation
     *
     * @param savedInstanceState the activity/fragment's savedInstanceState
     */
    private void setupTitle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentStepTitle = savedInstanceState.getString(EXTRA_STEP_TITLE);
            mTwoPane = savedInstanceState.getBoolean(EXTRA_TWO_PANE);
        } else {
            // No saved instance state, form the title using the item
            mCurrentStepTitle = getString(R.string.step, mStepItem.getStepNum()) + " "
                    + mStepItem.getShortDescription();
        }

        // Call activities to set the title of the app bar
        // two pane mode doesn't need a step-by-step title change
        if (!mTwoPane) {
            mListener.setTitle(mCurrentStepTitle);
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
        String thumbnailURLStr = mStepItem.getThumbnailURL();
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

            // Show the progress bar
            mVideoLoadingPb.setVisibility(View.VISIBLE);

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
     * TODO [ExoPlayer Media Playback ] Step 5 - Setup media style notification
     * for user to return to an active recipe step playback
     * <p>
     * Shows Media Style notification, with actions that depend on the current MediaSession
     * PlaybackState.
     *
     * @param state The PlaybackState of the MediaSession.
     */
    private void showNotification(PlaybackStateCompat state) {
        // Notification Content
        String mRecipeStepTitle = getString(R.string.current_step, mCurrentStepTitle);

        /* Create the pending content intent for the notification*/
        Intent intent = new Intent(mContext, StepDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID,mRecipeId);
        intent.putExtra(EXTRA_STEP_POSITION,mStepPosition);
        intent.putExtra(EXTRA_TWO_PANE_MODE, mTwoPane);

        PendingIntent mContentIntent = PendingIntent.getActivity(
                mContext,
                NOTIFICATION_PENDING_INTENT_RC,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Use notificationUtil to show notification, return the reference of the notification
        // manager for cancelling media notification tasks when the fragment no longer exists
        mNotificationManager = NotificationUtil.showMediaNotification(
                mContext,
                state,
                mMediaSession,
                mContentIntent,
                mRecipeName,
                mRecipeStepTitle,
                mRecipeThumbnailIcon
        );
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
        mRecipeThumbnailIcon = bitmap;
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
     * TODO [ExoPlayer Media Playback ] Step 7 - Handles when player changes states
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
        if (playbackState == Player.STATE_READY)
        {
            // Hide the video loading pb
            mVideoLoadingPb.setVisibility(View.GONE);

            if (playWhenReady)
            {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        mExoPlayer.getCurrentPosition(), 1f);
            }else
            {
                mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        mExoPlayer.getCurrentPosition(), 1f);
            }
        }

        mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification(mStateBuilder.build());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        mVideoLoadingPb.setVisibility(View.GONE);
        Toast.makeText(mContext,mContext.getString(R.string.exoplayer_playback_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

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
     * Fragment Life Cycle Callbacks and SavedInstance
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStepDetailFragmentInteraction) {
            mListener = (OnStepDetailFragmentInteraction) context;
        } else {
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
        outState.putString(EXTRA_STEP_TITLE, mCurrentStepTitle);
        outState.putBoolean(EXTRA_TWO_PANE, mTwoPane);
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
     * TODO [ExoPlayer Media Playback ] Step 8 - Handles external client controls via Receiver
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
     * TODO [ExoPlayer Media Playback ] Step 9 - Handles MediaSession callbacks
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
     * Interface callback to set host activity's title
     * if required
     */
    interface OnStepDetailFragmentInteraction {
        void setTitle(String title);
    }
}
