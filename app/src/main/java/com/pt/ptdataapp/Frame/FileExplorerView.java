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
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileExplorerView extends Fragment {
    private View rootView;
    private ListView mListView;
    private RelativeLayout mFileContentView;

    TextView IDLabel;
    TextView titleLabel;
    TextView patientNameLabel;
    TextView resultLabel;
    TextView doctorNameLabel;
    TextView checkDateLabel;
    TextView reportDateLabel;

    private MyFileAdapter mAdapter;
    private Context mContext;
    private File currentFile;
    private String rootFilePath;
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
            IDLabel = rootView.findViewById(R.id.printIDLabel);
            titleLabel = rootView.findViewById(R.id.printTitleLabel);
            patientNameLabel = rootView.findViewById(R.id.printPatientNameLabel);
            resultLabel = rootView.findViewById(R.id.printResultLabel);
            doctorNameLabel = rootView.findViewById(R.id.printDoctorNameLabel);
            checkDateLabel = rootView.findViewById(R.id.printCheckDateLabel);
            reportDateLabel = rootView.findViewById(R.id.printReportDateLabel);
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
//                            mListView.setVisibility(View.INVISIBLE);
//                            mFileContentView.setVisibility(View.VISIBLE);
//                            if (msg.obj != null)
//                            {
//                                ShowFileDetailInfo((String)msg.obj);
//                            }
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
        getData(rootFilePath);
    }

    private void getData(final String path) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.clear();
                mList.addAll(FileUtil.FindAllFile(path, false));
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
        menu.add(1,2,1,"复制");
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
                    final FileEntity entity = mList.get(menuSelectPosition);
                    if (entity != null)
                    {
                        String usbPath = "";
                        String destPath = LocalFileModel.DATA_PATH + File.separator + entity.getFileName();
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

    public void SaveEditData()
    {
        List<String> printList = new ArrayList<>();
        if (mFileContentView.getVisibility() == View.VISIBLE)
        {
            printList.add(titleLabel.getText().toString());
            printList.add(IDLabel.getText().toString());
            printList.add(patientNameLabel.getText().toString());
            printList.add(resultLabel.getText().toString());
            printList.add(doctorNameLabel.getText().toString());
            printList.add(checkDateLabel.getText().toString());
            printList.add(reportDateLabel.getText().toString());
        }
        else
        {
            Toast.makeText(Utils.getContext(), "请选择要打印的文件...", Toast.LENGTH_SHORT).show();
        }
        DataManager.getInstance().SavePrintContentList(printList);
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
        mFileContentView = rootView.findViewById(R.id.file_content_root);
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
