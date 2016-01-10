package com.jiuan.it.ipc.tools;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  AssetCopyer类
 *  实现将assets下的文件按目录结构拷贝到sdcard中
 *
 *  @author ticktick
 *  @Email lujun.hust@gmail.com
 */
public class AssetCopyer {

    private static final String ASSET_LIST_FILENAME = "assets.lst";

    private final Context mContext;
    private final AssetManager mAssetManager;
    private File mAppDirectory;

    public AssetCopyer(Context context) {
        mContext = context;
        mAssetManager = context.getAssets();
    }

    /**
     *  将assets目录下指定的文件拷贝到sdcard中
     *  @return 是否拷贝成功,true 成功；false 失败
     *  @throws IOException
     */
    public File copy() throws IOException {

        List<String> srcFiles = new ArrayList<String>();

        //获取系统在SDCard中为app分配的目录，eg:/sdcard/Android/data/$(app's package)
        //该目录存放app相关的各种文件(如cache，配置文件等)，unstall app后该目录也会随之删除
        mAppDirectory = mContext.getExternalFilesDir(null);//getDir("assets", Context.MODE_PRIVATE);
        if (null == mAppDirectory) {
            return mAppDirectory;
        }

        //读取assets/$(subDirectory)目录下的assets.lst文件，得到需要copy的文件列表
        List<String> assets = getAssetsList();
        for( String asset : assets ) {
            //如果不存在，则添加到copy列表
            if( ! new File(mAppDirectory,asset).exists() ) {
                srcFiles.add(asset);
            }
        }

        //依次拷贝到App的安装目录下
        for( String file : srcFiles ) {
            copy(file);
        }

        return mAppDirectory;
    }

    /**
     *  获取需要拷贝的文件列表（记录在assets/assets.lst文件中）
     *  @return 文件列表
     *  @throws IOException
     */
    protected List<String> getAssetsList() throws IOException {

        List<String> files = new ArrayList<String>();

        InputStream listFile = mAssetManager.open(new File(ASSET_LIST_FILENAME).getPath());
        BufferedReader br = new BufferedReader(new InputStreamReader(listFile));
        String path;
        while (null != (path = br.readLine())) {
            files.add(path);
        }

        return files;
    }

    /**
     *  执行拷贝任务
     *  @param asset 需要拷贝的assets文件路径
     *  @return 拷贝成功后的目标文件句柄
     *  @throws IOException
     */
    protected File copy( String asset ) throws IOException {

        InputStream source = mAssetManager.open(new File(asset).getPath());
        File destinationFile = new File(mAppDirectory, asset);
        destinationFile.getParentFile().mkdirs();
        OutputStream destination = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int nread;

        while ((nread = source.read(buffer)) != -1) {
            if (nread == 0) {
                nread = source.read();
                if (nread < 0)
                    break;
                destination.write(nread);
                continue;
            }
            destination.write(buffer, 0, nread);
        }
        destination.close();

        return destinationFile;
    }
}