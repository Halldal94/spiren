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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;

    private ViewRenderable renderrable, statusBarRenderable;
    private ImageView imgView;
    private Plant plant;
    private TextView height;
    private ProgressBar health, water, fertalizer;
    private ImageButton waterBtn, bugSprayBtn, fertalizeBtn, helpBtn;
    private View statusBarView;

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

        //Setting up buttons
        waterBtn = (ImageButton) findViewById(R.id.watercanbutton);
        bugSprayBtn = (ImageButton) findViewById(R.id.bugspraybutton);
        fertalizeBtn = (ImageButton) findViewById(R.id.fertilizerbutton);

        helpBtn = (ImageButton) findViewById(R.id.helpbutton);

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
                    health = statusBarView.findViewById(R.id.health);
                    water = statusBarView.findViewById(R.id.water);
                    fertalizer = statusBarView.findViewById(R.id.fertelizer);
                });

        //AugmentedImageDatabase imageDatabase = new AugmentedImageDatabase(session);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (statusBarRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    Node andy = new Node();
                    andy.setParent(anchorNode);
                    andy.setRenderable(statusBarRenderable);
                    updateInfo();
                });
        /*arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                });*/


        /*
        * Test "status bar"
        *
        ViewRenderable.builder()
                .setView(this, R.layout.status_bar)
                .build()
                .thenAccept(renderable -> {
                    ImageView imgView = (ImageView)renderrable.getView();
                });
        */

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
