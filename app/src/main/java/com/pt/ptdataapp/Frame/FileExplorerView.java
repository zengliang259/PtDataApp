package com.pt.ptdataapp.Frame;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pt.ptdataapp.MainActivity;
import com.pt.ptdataapp.Model.DataManager;
import com.pt.ptdataapp.Model.FileEntity;
import com.pt.ptdataapp.Model.LocalFileModel;
import com.pt.ptdataapp.Model.PatientInfo;
import com.pt.ptdataapp.R;
import com.pt.ptdataapp.fileUtil.FileDataReader;
import com.pt.ptdataapp.fileUtil.FileUtil;
import com.pt.ptdataapp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileExplorerView extends Fragment {
    private View rootView;
    private ListView mListView;

    TextView IDLabel;
    TextView titleLabel;
    TextView patientNameLabel;
    TextView resultLabel;
    TextView doctorNameLabel;
    TextView checkDateLabel;
    TextView reportDateLabel;

    private MyFileAdapter mAdapter;
    private MainActivity mContext;
    private File currentFile;
    private String rootFilePath;
    private int parentPosition = 0;
    private boolean bInit = false;

    private ArrayList<FileEntity> mList;

    private int menuSelectPosition = -1;

    private Handler mHandler;
    public FileExplorerView() {
        // Required empty public constructor
    }
    //1、定义接口
    public interface OnFileClick {
        public void onClick(File clickFile);
    }
    private OnFileClick onFileClick;//2、定义接口成员变量
    //定义接口变量的get方法
    public OnFileClick getOnFileClick() {
        return onFileClick;
    }
    //定义接口变量的set方法
    public void setOnFileClick(OnFileClick onFileClick) {
        this.onFileClick = onFileClick;
    }




    public void SetContext(MainActivity context)
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
        UpdateUI();
        return rootView;
    }
    public void Refresh(String filePath, File curFile, int position)
    {
        rootFilePath = filePath;
        currentFile = curFile;
        if (position >= 0)
        {
            parentPosition = position;
        }
        System.out.println(rootFilePath);
    }

    private void UpdateUI()
    {
        if (rootView != null)
        {
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
                                if(mAdapter ==null){
                                    mAdapter = new MyFileAdapter(mContext, mList);
                                    mListView.setAdapter(mAdapter);
                                }else{
                                    mAdapter.notifyDataSetChanged();
                                }

                                break;
                            case 2:
                                if(onFileClick !=null){
                                    onFileClick.onClick((File)msg.obj);
                                }

                                break;

                            default:
                                break;
                        }
                    }
                };
            }
            if (currentFile != null)
            {
                getData(currentFile.getPath());
            }
            else
            {
                if (rootFilePath != null)
                {
                    getData(rootFilePath);
                }
            }

        }
    }

    private void getData(final String path) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.clear();
                mList.addAll(FileUtil.FindAllFile(path, false));
                Collections.sort(mList, new Comparator<FileEntity>() {
                    @Override
                    public int compare(FileEntity o1, FileEntity o2) {
                        String fileName1 = o1.getFileName().replace(".txt", "");
                        String fileName2 = o2.getFileName().replace(".txt", "");
                        if (fileName1.equals("id"))
                        {
                            fileName1 = "0";
                        }
                        if (fileName2.equals("id"))
                        {
                            fileName2 = "0";
                        }
                        int num1 = Integer.parseInt(fileName1, 10);
                        int num2 = Integer.parseInt(fileName2, 10);
                        return -num1 + num2;
                    }
                });
                mHandler.sendEmptyMessage(1);
            }
        });

    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        menuSelectPosition = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
        //设置Menu显示内容
        menu.setHeaderTitle("文件操作");
//        menu.setHeaderIcon(R.drawable.ic_launcher_foreground);
        menu.add(1,1,1,"删除");
        File curCopyTargetUsbFile = DataManager.getInstance().GetCopyUsbPath();
        if (curCopyTargetUsbFile != null)
        {
            menu.add(1,2,1,"复制至" + curCopyTargetUsbFile.getName());
        }

    }

    public boolean onContextItemSelected(MenuItem item){
        switch(item.getItemId()){
            case 1:
                if (menuSelectPosition >= 0)
                {
                    final FileEntity entity = mList.get(menuSelectPosition);
                    if (entity != null)
                    {
                        if (entity.getFileType() == FileEntity.Type.FILE)
                        {
                            FileUtil.deletefile(entity.getFilePath());
                        }
                        else
                        {
                            FileUtil.deleteDirectory(entity.getFilePath());
                        }

                        mList.remove(menuSelectPosition);
                        menuSelectPosition = -1;
                        mHandler.sendEmptyMessage(1);
                    }
                }

                break;
            case 2:
                if (menuSelectPosition >= 0)
                {
                    File curCopyTargetUsbFile = DataManager.getInstance().GetCopyUsbPath();
                    if (curCopyTargetUsbFile == null || !curCopyTargetUsbFile.exists())
                    {
                        Toast.makeText(Utils.getContext(),"复制目标目录不存在", Toast.LENGTH_SHORT).show();
                        return super.onContextItemSelected(item);
                    }

                    final FileEntity entity = mList.get(menuSelectPosition);
                    if (entity != null)
                    {
                        String sourcePath = entity.getFilePath();
                        String targetPath = curCopyTargetUsbFile.getAbsolutePath() + File.separator + entity.getFileName();
                        mContext.ShowDialog("正在复制文件,请勿拔出USB设备");
                        if (entity.getFileType() == FileEntity.Type.FLODER) {

                            FileUtil.copyFolder(sourcePath, targetPath, null);
                        }
                        else
                        {
                            FileUtil.copyFile(sourcePath, targetPath);
                        }
                        mContext.HideDialog();
                        menuSelectPosition = -1;
                    }
                }
                break;
        }
        return super.onContextItemSelected(item);
    }


    private void ShowFileDetailInfo(String fileContent)
    {
        PatientInfo pInfo = FileDataReader.Read(fileContent);
        titleLabel.setText(pInfo.title);
        IDLabel.setText(pInfo.ID);
        resultLabel.setText(pInfo.checkResult);
        checkDateLabel.setText(pInfo.checkDate);
        reportDateLabel.setText(pInfo.reportDate);
        int inputType = (pInfo.patientName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
        patientNameLabel.setInputType(inputType);
        patientNameLabel.setText(pInfo.patientName);
        inputType = (pInfo.doctorName.length() > 0) ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT;
        doctorNameLabel.setInputType(inputType);
        doctorNameLabel.setText(pInfo.doctorName);
    }


    public void onBackPressed() {
        if(rootFilePath.equals(currentFile.getAbsolutePath())){
            System.out.println("已经到了根目录...");
            if (mContext != null)
            {
                mContext.OnShowStationListPage(parentPosition);
            }
            return ;
        }

        String parentPath = currentFile.getParent();
        currentFile = new File(parentPath);
        getData(parentPath);
    }

    private void initView() {
        mListView =  rootView.findViewById(R.id.file_list_view);
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
                            if(!entity.getFileName().contains("id.txt"))
                            {
//                                String fileContent = FileUtil.getEncryptFile(entity.getFilePath());
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = new File(entity.getFilePath());
                                mHandler.sendMessage(msg);
                            }
                            else
                            {
                                String fileContent = FileUtil.getFile(entity.getFilePath());
                                Toast.makeText(Utils.getContext(), "设备ID:"+fileContent, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                return false;
//            }
//        });
        registerForContextMenu(mListView);
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
                    holder.tv.setText(entity.getFileName().replace(".txt", ""));

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
