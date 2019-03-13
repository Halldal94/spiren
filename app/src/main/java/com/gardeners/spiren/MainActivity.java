package com.gardeners.spiren;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;

    private ModelRenderable flowerRenderable;
    private ModelRenderable leafRenderable;
    private ModelRenderable potRenderable;
    private ModelRenderable stalkRenderable;
    private ViewRenderable renderrable, statusBarRenderable;
    private ImageView imgView;
    private Plant plant;
    private TextView height, bugCount;
    private ProgressBar health, water, fertalizer;
    private ImageButton waterBtn, bugSprayBtn, fertalizeBtn, helpBtn;
    private View statusBarView;
    private Button grow, bugs, action;

    private boolean developerMode = false;

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

        //Setting up text and progress bars
        plant = new Plant(this);
        plant.growTimer();
        //loadData();

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
        fertalizeBtn = (ImageButton) findViewById(R.id.fertilizerbutton);

        helpBtn = (ImageButton) findViewById(R.id.helpbutton);

        if (developerMode) {
            setUpDeveloperEnv();
        }

        waterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                plant.waterPlant();
                updateInfo();
            }
        });

        fertalizeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                plant.fertelizerPlant();
                updateInfo();
            }
        });

        helpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO
            }
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
                    fertalizer = statusBarView.findViewById(R.id.fertelizer);
                });

        // Denne tegner status bar PS: husk og ikke opdatere verdier før denne er tegnet
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (statusBarRenderable == null) {
                        return;
                    }

                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Node statusBar = new Node();
                    statusBar.setParent(anchorNode);
                    statusBar.setRenderable(statusBarRenderable);
                    updateInfo();
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

                    plantView = new PlantView(leafRenderable, pot, stalk, flowerNode, 0xDEADBEEFDEADBEEFL);
                    plantView.setHeight(heightSlider.getProgress());
                });
    }

    private void setUpDeveloperEnv() {
        grow = (Button) findViewById(R.id.grow);
        bugs = (Button) findViewById(R.id.bugs);
        action = (Button) findViewById(R.id.action);

        grow.setVisibility(View.VISIBLE);
        bugs.setVisibility(View.VISIBLE);
        action.setVisibility(View.VISIBLE);

        grow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(developerMode){
                    plant.grow();
                }
            }
        });

        bugs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(developerMode){
                    plant.bugSpawnerDev();
                    updateInfo();
                }
            }
        });

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(developerMode){
                    plant.resetPreviusAction();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */

    public void updateInfo(){
        if(health != null){
            height.setText(String.valueOf(plant.getLength()) + " cm");
            health.setProgress(plant.getHealth());
            water.setProgress(plant.getWater());
            fertalizer.setProgress(plant.getFertelizer());
            bugCount.setText(plant.getBugs() + " Bugs");
        }
    }

    private void saveData(){
        String filename = "save";
        JSONObject content = new JSONObject();
        try {
            content.put("length", plant.getLength());
            content.put("health", plant.getHealth());
            content.put("water", plant.getWater());
            content.put("fertelizer", plant.getFertelizer());
            content.put("bugs", plant.getBugs());
            content.put("members", plant.getMembers());
            content.put("level", plant.getLevel());
            content.put("previus", plant.getPrevius().toString());
            try {
                FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(content.toString().getBytes());
                outputStream.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        String filename = "save";
        File directory = this.getFilesDir();
        File file = new File(directory, filename);
        if(file != null || file.exists()) {
            try {
                byte[] encoded = Files.readAllBytes(file.toPath());
                JSONObject load = new JSONObject(new String(encoded));
                plant.loadData(load);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


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
