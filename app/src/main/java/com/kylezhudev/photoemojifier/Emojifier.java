package com.kylezhudev.photoemojifier;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import timber.log.Timber;


public class Emojifier {
    private static final double SMILING_PROB_THRESHOLD = 0.15;
    private static final double EYE_OPEN_PROB_THRESHOLD = 0.5;
    private static final float EMOJI_SCALE_FACTOR = 0.8f;






    public static Bitmap detectFacesAndOverlayEmoji(Context context, Bitmap imageBitmap){
        if (imageBitmap == null){
            return null;
        }
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
        SparseArray<Face> faces = detector.detect(frame);

        Timber.d("detectFaces: number of faces = " + faces.size());

        Bitmap resultBitmap = imageBitmap;

        if (faces.size() == 0){
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show();
        }else{
            for (int i = 0; i < faces.size(); i++){

                Face face = faces.valueAt(i);
                Emoji emoji = whichEmoji(face);

                Bitmap emojiBitmap;
                switch (emoji){
                    case SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
                        break;
                    case FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWNING:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                }

                resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap,face);
            }

        }
        detector.release();
        return resultBitmap;

    }

    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {
        Bitmap overlaidBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(),backgroundBitmap.getConfig());

        int newEmojiWidth = (int) (face.getWidth() * EMOJI_SCALE_FACTOR);
        int newEmojiHeight = (int) (face.getHeight() * newEmojiWidth / face.getWidth() * EMOJI_SCALE_FACTOR);

        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        float emojiPositionX = (face.getPosition().x + face.getWidth()) / 2 - emojiBitmap.getWidth() / 2 ;
        float emojiPositionY = (face.getPosition().y + face.getHeight()) /2 - emojiBitmap.getHeight() / 5;
        Canvas canvas = new Canvas(overlaidBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);


        return overlaidBitmap;
    }

    public static Emoji whichEmoji(Face face){
        float leftEyeOpenProbability = face.getIsLeftEyeOpenProbability();
        float rightEyeOpenProbability = face.getIsRightEyeOpenProbability();
        float smilingProbability = face.getIsSmilingProbability();

        Timber.d("Left Eye Open Probability: " + leftEyeOpenProbability
                + "Right Eye Open Probability: " + rightEyeOpenProbability
                + "Smiling Probability: " + smilingProbability);

        boolean smiling = smilingProbability > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = leftEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = rightEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD;

        Emoji emoji;
        if(smiling){
            if(leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            }else if(!leftEyeClosed && rightEyeClosed){
                emoji = Emoji.LEFT_WINK;
            }else if(leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILING;
            }else{
                emoji = Emoji.SMILING;
            }
        }else{
            if(leftEyeClosed && !rightEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWNING;
            }else if(!leftEyeClosed && rightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWNING;
            }else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWNING;
            }else{
                emoji = Emoji.FROWNING;
            }
        }

        Timber.d("whichEmoji: " + emoji.name());

        return emoji;

    }

    enum Emoji {
        SMILING, FROWNING, LEFT_WINK, RIGHT_WINK, LEFT_WINK_FROWNING, RIGHT_WINK_FROWNING, CLOSED_EYE_SMILING, CLOSED_EYE_FROWNING
    }



}
