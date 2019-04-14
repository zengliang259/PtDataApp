package com.pt.ptdataapp.Frame;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.fileUtil.FileUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileExplorerView extends Fragment {
    private View rootView;
    private ListView mListView;
    private TextView mFileContentView;
    private MyFileAdapter mAdapter;
    private Context mContext;
    private File currentFile;
    private String rootFilePath;
    private boolean bInit = false;

    private ArrayList<FileEntity> mList;

    private Handler mHandler;
    public FileExplorerView() {
        // Required empty public constructor
    }
    public void SetContext(Context context)
    {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null)
        {
            rootView = inflater.inflate(R.layout.fragment_file_explorer_view, container, false);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        Init();
        return rootView;
    }

    private void Init()
    {
        currentFile = new File(Environment.getExternalStorageDirectory(), LocalFileModel.DATA_PATH);
        rootFilePath = currentFile.getAbsolutePath();
        System.out.println(rootFilePath);
        if (!bInit)
        {
            bInit = true;
            mList = new ArrayList<>();
            initView();
            mHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 1:
                            mListView.setVisibility(View.VISIBLE);
                            mFileContentView.setVisibility(View.INVISIBLE);
                            if(mAdapter ==null){
                                mAdapter = new MyFileAdapter(mContext, mList);
                                mListView.setAdapter(mAdapter);
                            }else{
                                mAdapter.notifyDataSetChanged();
                            }

                            break;
                        case 2:
                            mListView.setVisibility(View.INVISIBLE);
                            mFileContentView.setVisibility(View.VISIBLE);
                            if (msg.obj != null)
                            {
                                mFileContentView.setText((String)msg.obj);
                            }

                            break;

                        default:
                            break;
                    }
                }
            };
        }
        getData(rootFilePath);
    }

    private void getData(final String path) {
        new Thread(){
            @Override
            public void run() {
                super.run();

                findAllFiles(path);
            }
        }.start();

    }
    public void findAllFiles(String path) {
        mList.clear();
        if(path ==null ||path.equals("")){
            return;
        }
        File fatherFile = new File(path);
        File[] files = fatherFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                FileEntity entity = new FileEntity();
                boolean isDirectory = files[i].isDirectory();
                if(isDirectory ==true){
                    entity.setFileType(FileEntity.Type.FLODER);
                }else{
                    entity.setFileType(FileEntity.Type.FILE);
                }
                entity.setFileName(files[i].getName().toString());
                entity.setFilePath(files[i].getAbsolutePath());
                entity.setFileSize(files[i].length()+"");
                mList.add(entity);
            }
        }
        mHandler.sendEmptyMessage(1);
    }


    public void onBackPressed() {
        if (mFileContentView.getVisibility() == View.VISIBLE)
        {
            mListView.setVisibility(View.VISIBLE);
            mFileContentView.setVisibility(View.INVISIBLE);
//            mHandler.sendEmptyMessage(1);
        }
        else
        {
            if(rootFilePath.equals(currentFile.getAbsolutePath())){
                System.out.println("已经到了根目录...");
                return ;
            }

            String parentPath = currentFile.getParent();
            currentFile = new File(parentPath);
            getData(parentPath);
        }

    }

    private void initView() {
        mListView =  rootView.findViewById(R.id.file_list_view);
        mFileContentView = rootView.findViewById(R.id.file_content_label);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final FileEntity entity = mList.get(position);
                if(entity.getFileType() == FileEntity.Type.FLODER){
                    currentFile = new File(entity.getFilePath());
                    // 显示文件列表
                    getData(entity.getFilePath());
                }else if(entity.getFileType() == FileEntity.Type.FILE){
                    ((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String fileContent = FileUtil.getFile(entity.getFilePath());
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = fileContent;
                            mHandler.sendMessage(msg);
                        }
                    });
                }

            }
        });
    }

    class MyFileAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<FileEntity> mAList;
        private LayoutInflater mInflater;



        public MyFileAdapter(Context mContext, ArrayList<FileEntity> mList) {
            super();
            this.mContext = mContext;
            this.mAList = mList;
            mInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mAList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mAList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if(mAList.get(position).getFileType() == FileEntity.Type.FLODER){
                return 0;
            }else{
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//          System.out.println("position-->"+position+"  ---convertView--"+convertView);
            ViewHolder holder = null;
            int type = getItemViewType(position);
            FileEntity entity = mAList.get(position);

            if(convertView == null){
                holder = new ViewHolder();
                switch (type) {
                    case 0://folder
                        convertView = mInflater.inflate(R.layout.item_file_info, parent, false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);
                        break;
                    case 1://file
                        convertView = mInflater.inflate(R.layout.item_file_info, parent, false);
                        holder.iv = (ImageView) convertView.findViewById(R.id.item_imageview);
                        holder.tv = (TextView) convertView.findViewById(R.id.item_textview);

                        break;

                    default:
                        break;

                }
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            switch (type) {
                case 0:
                    holder.iv.setImageResource(R.drawable.floder_img);
                    holder.tv.setText(entity.getFileName());
                    break;
                case 1:
                    holder.iv.setImageResource(R.drawable.file_img);
                    holder.tv.setText(entity.getFileName());

                    break;

                default:
                    break;
            }


            return convertView;
        }

    }

    class ViewHolder {
        ImageView iv;
        TextView tv;
    }

}
