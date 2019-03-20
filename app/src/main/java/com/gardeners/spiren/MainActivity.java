package com.gardeners.spiren;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;

    private SoundPool soundPool;

    private Random sndRandom = new Random();
    private int[] waterSnds, bugSpraySnds, fertilizeSnds;

    private ModelRenderable flowerRenderable;
    private ModelRenderable leafRenderable;
    private ModelRenderable potRenderable;
    private ModelRenderable stalkRenderable;
    private ViewRenderable renderrable, statusBarRenderable;
    private ImageView imgView;
    private TextView height, bugCount;
    private ProgressBar health, water, fertilizer;
    private ImageButton waterBtn, bugSprayBtn, fertilizeBtn, helpBtn;
    private View statusBarView;
    private Button grow, bugs, action;

    private boolean developerMode = false;

    private PlantModel plantModel;
    private PlantController plantController;
    private PlantView plantView;

    private SeekBar heightSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        //Setting up AR view
        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        waterSnds = new int[] {
                soundPool.load(this, R.raw.water_1, 1),
                soundPool.load(this, R.raw.water_2, 1)
        };
        bugSpraySnds = new int[] {
                soundPool.load(this, R.raw.spray_1, 1)
        };
        fertilizeSnds = new int[] {
                soundPool.load(this, R.raw.fertilize_1, 1),
                soundPool.load(this, R.raw.fertilize_2, 1)
        };

        plantModel = new PlantModel();
        plantController = new PlantController(plantModel, this);
        plantController.initialize();
        plantController.growTimer();

        heightSlider = (SeekBar) findViewById(R.id.heightSlider);
        heightSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (plantView != null) {
                    plantView.setHeight(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        updateInfo();

        //Setting up buttons
        waterBtn = (ImageButton) findViewById(R.id.watercanbutton);
        bugSprayBtn = (ImageButton) findViewById(R.id.bugspraybutton);
        fertilizeBtn = (ImageButton) findViewById(R.id.fertilizerbutton);

        helpBtn = (ImageButton) findViewById(R.id.helpbutton);

        if (developerMode) {
            setUpDeveloperEnv();
        }

        waterBtn.setOnClickListener(v -> {
            playInteractionSound(waterSnds);
            plantController.water();
            updateInfo();
        });

        bugSprayBtn.setOnClickListener(v -> {
            playInteractionSound(bugSpraySnds);
            plantController.killBugs();
            updateInfo();
        });

        fertilizeBtn.setOnClickListener(v -> {
            playInteractionSound(fertilizeSnds);
            plantController.fertilize();
            updateInfo();
        });

        helpBtn.setOnClickListener(v -> {
            //TODO
        });

        ViewRenderable.builder()
                .setView(this, R.layout.status_bar)
                .build()
                .thenAccept(renderable -> statusBarRenderable = renderable)
                .thenAccept(renderrable -> {
                    statusBarView = statusBarRenderable.getView();
                    height = statusBarView.findViewById(R.id.height);
                    bugCount = statusBarView.findViewById(R.id.bug_count);
                    health = statusBarView.findViewById(R.id.health);
                    water = statusBarView.findViewById(R.id.water);
                    fertilizer = statusBarView.findViewById(R.id.fertelizer);
                });

        CompletableFuture<ModelRenderable> flowerFuture = ModelRenderable.builder().setSource(this, R.raw.flower).build();
        CompletableFuture<ModelRenderable> leafFuture = ModelRenderable.builder().setSource(this, R.raw.leaf).build();
        CompletableFuture<ModelRenderable> potFuture = ModelRenderable.builder().setSource(this, R.raw.pot).build();
        CompletableFuture<ModelRenderable> stalkFuture = ModelRenderable.builder().setSource(this, R.raw.stalk).build();
        CompletableFuture.allOf(flowerFuture, leafFuture, potFuture, stalkFuture)
                // .thenApply(future -> new ModelRenderable[] { flowerFuture.join(), potFuture.join(), stalkFuture.join() })
                .thenRun(() -> {
                    this.flowerRenderable = flowerFuture.join();
                    this.leafRenderable = leafFuture.join();
                    this.potRenderable = potFuture.join();
                    this.stalkRenderable = stalkFuture.join();
                })
                .exceptionally(throwable -> {
                    Toast toast =
                            Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (stalkRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable pot and add it to the anchor.
                    TransformableNode pot = new TransformableNode(arFragment.getTransformationSystem());
                    pot.setLocalPosition(new Vector3(0.0F, 0.25F, 0.0F));
                    pot.setParent(anchorNode);
                    pot.setRenderable(potRenderable);
                    pot.select();

                    Node stalk = new Node();
                    stalk.setLocalPosition(new Vector3(0.0F, 0.0F, 0.0F));
                    stalk.setParent(pot);
                    stalk.setRenderable(stalkRenderable);

                    Node flowerNode = new Node();
                    flowerNode.setLocalRotation(Quaternion.axisAngle(new Vector3(1.0F, 0.0F, 0.0F), 70.0F));
                    flowerNode.setParent(pot);
                    flowerNode.setRenderable(flowerRenderable);

                    Node statusNode = new Node();
                    statusNode.setLocalPosition(new Vector3(0.0F, 0.0F, 0.0F));
                    statusNode.setLocalScale(new Vector3(0.7F, 0.7F, 0.7F));
                    statusNode.setParent(pot);
                    statusNode.setRenderable(statusBarRenderable);

                    updateInfo();

                    plantView = new PlantView(leafRenderable, pot, stalk, flowerNode, statusNode, 0xDEADBEEFDEADBEEFL);
                    plantView.setHeight(plantModel.getHeight());
                });
    }

    private void playInteractionSound(int[] snds) {
        int snd = snds[sndRandom.nextInt(snds.length)];
        soundPool.play(snd, 1.0F, 1.0F, 1, 0, 1.0F);
    }

    private void setUpDeveloperEnv() {
        grow = (Button) findViewById(R.id.grow);
        bugs = (Button) findViewById(R.id.bugs);
        action = (Button) findViewById(R.id.action);

        grow.setVisibility(View.VISIBLE);
        bugs.setVisibility(View.VISIBLE);
        action.setVisibility(View.VISIBLE);

        grow.setOnClickListener(v -> {
            if(developerMode){
                plantController.grow();
            }
        });

        bugs.setOnClickListener(v -> {
            if(developerMode){
                plantController.bugSpawnerDev();
                updateInfo();
            }
        });

        action.setOnClickListener(v -> {
            if(developerMode){
                plantController.resetPreviousAction();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    public void updateInfo(){
        if(health != null){
            height.setText(String.valueOf(plantModel.getHeight()) + " cm");
            health.setProgress(plantModel.getHealth());
            water.setProgress(plantModel.getWater());
            fertilizer.setProgress(plantModel.getFertilizer());
            bugCount.setText(plantModel.getBugs() + " Bugs");
        }
        if (plantView != null) {
            plantView.setHeight(plantModel.getHeight());
        }
    }

    private void saveData(){
        String filename = "save";
        JSONObject data = plantModel.toJson();
        try {
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.toString().getBytes(Charset.forName("UTF-8")));
            outputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadData() {
        String filename = "save";
        File directory = this.getFilesDir();
        File file = new File(directory, filename);
        if(file.exists()) {
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                JSONObject data = new JSONObject(new String(encoded, Charset.forName("UTF-8")));
                plantModel.fromJson(data);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
