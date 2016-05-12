package cc.seeed.iot.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.Header;

public class ImgUtil {
    public static String filePath ;

    public static void displayImage(SimpleDraweeView draweeView, String uuid, String urlDefault) {
        if (!TextUtils.isEmpty(uuid)) {
            draweeView.setImageURI(getImageUrl(uuid));
        } else if (!TextUtils.isEmpty(uuid)) {
            draweeView.setImageURI(Uri.parse(urlDefault));
        }
    }

    public static void displayImg(SimpleDraweeView draweeView, String uuid, int default_res) {
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(default_res))
                .build();
        displayImage(draweeView, getImageUrl(uuid), uri,uuid);
    }

    public static void displayImgFormFile(SimpleDraweeView draweeView, String uuid) {
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_FILE_SCHEME) // "res"
                .path(uuid)
                .build();
        displayImage(draweeView, uri, null,uuid);
    }

    public static void displayBitmap(SimpleDraweeView draweeView, Bitmap bitmap) {
        draweeView.setImageBitmap(bitmap);
    }

    public static void displayImage(SimpleDraweeView draweeView, Uri uri, Uri urlDefault,String filePath) {
        if (uri != null) {
            draweeView.setImageURI(uri);
        } else {
            draweeView.setImageURI(urlDefault);
        }

   /*     DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri)
                .build();
        draweeView.setController(controller);
*/
     //   String fileName = ToolUtil.getFileName(filePath);
      //  getBitmap(uri, draweeView,fileName);

    }


    public static Uri getImageUrl(String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            String s = uuid;
            if (uuid.startsWith("/")) {
                s = uuid.replaceFirst("/", "");
            }else if (uuid.startsWith("http://")){
                return Uri.parse(uuid);
            }
//            return Uri.parse(ConstantUrl.Image_Prefix.getVal() + s);
            return null;
        } else {
            return null;
        }
    }

    static ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable anim) {
            Log.d("TAG", "onFinalImageSet: id " + id + " imageInfo: " + imageInfo.toString());
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
           // App.showToast("onIntermediateImageSet");
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
           // App.showToast("onFailure");

        }
    };


    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }


    public static void getBitmap(Uri uri, final SimpleDraweeView draweeView, final String fileName) {

        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, draweeView.getContext());

        dataSource.subscribe(new BaseBitmapDataSubscriber() {

                                 @Override
                                 public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                     Log.d("TAG", "onNewResultImpl " + bitmap.toString());
                                    // String fileName = System.currentTimeMillis()+".jpg";
                                    /* boolean isSaveSuccess = ToolUtil.saveFile(bitmap, Constant.ImageSavePath, fileName);

                                     if (TextUtil.isEmpty(filePath) && isSaveSuccess){
                                         filePath = ToolUtil.getSDPath()+Constant.ImageSavePath+fileName;
                                     }*/

                                 }

                                 @Override
                                 public void onFailureImpl(DataSource dataSource) {
                                     // No cleanup required here.
                                     Log.d("TAG", "onFailureImpl " );
                                 }
                             }

                ,
                CallerThreadExecutor.getInstance());
    }

    public void postFile() throws Exception{
        String path ="";
        File file = new File(path);
        if(file.exists() && file.length()>0){
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("profile_picture", file);
            client.post("http://115.28.73.39/web", params,new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
              //      Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      byte[] responseBody, Throwable error) {
                  //  Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_LONG).show();
                }
            });
        }else{
          //  Toast.makeText(this, "文件不存在", 1).show();
        }

    }

    /**
     * 根据路径加载bitmap
     *
     * @param path
     *            路径
     * @param w
     *            宽
     * @param h
     *            长
     * @return
     */
    public static final Bitmap convertToBitmap(String path, int w, int h) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            // 设置为ture只获取图片大小
            opts.inJustDecodeBounds = true;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // 返回为空
            BitmapFactory.decodeFile(path, opts);
            int width = opts.outWidth;
            int height = opts.outHeight;
            float scaleWidth = 0.f, scaleHeight = 0.f;
            if (width > w || height > h) {
                // 缩放
                scaleWidth = ((float) width) / w;
                scaleHeight = ((float) height) / h;
            }
            opts.inJustDecodeBounds = false;
            float scale = Math.max(scaleWidth, scaleHeight);
            opts.inSampleSize = (int) scale;
            WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
            Bitmap bMapRotate = Bitmap.createBitmap(weak.get(), 0, 0, weak.get().getWidth(), weak.get().getHeight(), null, true);
            if (bMapRotate != null) {
                return bMapRotate;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class CompressInfo {
        public long fileSize;
        public String path;
        public String md5;
        public int width;
        public int height;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > width) {
            int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(heightRatio, widthRatio);
        } else {
            int heightRatio = Math.round((float) height
                    / (float) reqWidth);
            int widthRatio = Math.round((float) width / (float) reqHeight);
            inSampleSize = Math.max(heightRatio, widthRatio);
        }
        inSampleSize = Math.max(inSampleSize, 1);
      //  QLog.d("calcSize", "h:" + height + " w " + width + " sample " + inSampleSize);
        return inSampleSize;
    }


    /**
     * 压缩图片
     * @param filePath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static CompressInfo compressBitmap(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int oriW = options.outWidth;
        int oriH = options.outHeight;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        int sW = options.outWidth / options.inSampleSize;
        int sH = options.outHeight / options.inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            return null;
        }

        String fileName = "";
        String[] split = filePath.split("\\.");
        if (split != null && split.length > 0){
            fileName = System.currentTimeMillis() + "."+split[split.length -1];
        }

        String path = Common.ImgPath + fileName;
        File fileDir = new File(Common.AppRootPath);
        if (!fileDir.exists()){
            fileDir.mkdirs();
        }
        File temp = new File(path);
        if (!temp.exists()){
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(temp);
            if (bm.compress(Bitmap.CompressFormat.JPEG, 70, out)) {
                out.flush();
                out.close();
                bm.recycle();
                String md5 = FileUtil.fileMD5(path);
                if (md5 == null)
                    return null;
                String targetPath =  Common.ImgPath + md5;
                File target = new File(targetPath);
                if (target.exists())
                    target.delete();
                temp.renameTo(new File(targetPath));
                CompressInfo info = new CompressInfo();
                info.fileSize = new File(targetPath).length();
                info.md5 = md5;
                info.path = targetPath;
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(targetPath, options2);
                info.width = options2.outWidth;
                info.height = options2.outHeight;
              //  QLog.d("imgutil", "sw:" + sW + " sh:" + sH + " w:" + options2.outWidth + " h:" + options2.outHeight + " oW:" + oriW + " oH:" + oriH);
                return info;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File f = new File(path);
            if (f.exists())
                f.delete();
        }
        return null;
    }

}
