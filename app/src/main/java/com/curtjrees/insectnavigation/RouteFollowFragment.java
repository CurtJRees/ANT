package com.curtjrees.insectnavigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * A placeholder fragment containing a simple view.
 */
public class RouteFollowFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView error_label;

    Mat goalImage = new Mat();

    public RouteFollowFragment() {
    }

    static {
        // If you use opencv 2.4, System.loadLibrary("opencv_java")
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mOpenCvCameraView.enableView();

                    Mat temp = new Mat();
                    Utils.bitmapToMat(MainActivity.bitmap, temp);

                    Imgproc.cvtColor(temp, temp, Imgproc.COLOR_RGB2GRAY);
//                    Core.flip(temp, temp, 1);

                    Size size = new Size(1920,1080);
                    Imgproc.resize(temp, goalImage, size);

                    break;

                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getContext(), mLoaderCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_routefollow, container, false);

        error_label = (TextView) view.findViewById(R.id.error_label);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) view.findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);



        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat currentImage = inputFrame.gray();

//        sq_error = abs(img_ref - img_current) .^ 2;
//        rms_error = sqrt(sum(sq_error(:)) / numel(img_ref));


        if ((goalImage != null) & (currentImage != null)) {
            if ((currentImage.height() == goalImage.height()) & (currentImage.width() == goalImage.width())) {
                Mat difference = Mat.zeros(currentImage.width(), currentImage.height(), currentImage.type());
                Core.absdiff(currentImage, goalImage, difference);

                Scalar diff = Core.sumElems(difference);

                double squareError = Math.pow(diff.val[0], 2);
                final double rmsError = Math.sqrt((squareError / (difference.height() * difference.width())));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error_label.setText("RMSE: " + rmsError);
                    }
                });
            }
        }

        return inputFrame.rgba();
    }

}
