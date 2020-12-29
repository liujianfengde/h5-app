package cn.liuxiaoer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.liuxiaoer.adapter.GroupRecyclerAdapter;
import cn.liuxiaoer.util.FileGroup;
import cn.liuxiaoer.util.FileUtil;
import cn.liuxiaoer.util.Member;

import static cn.liuxiaoer.util.FileUtil.DOWNLOAD_PATH;
import static cn.liuxiaoer.util.FileUtil.packageFileGroup;

public class DownloadFileActivity extends Activity implements ExpandableListView.OnChildClickListener {

    private RecyclerView downloadListView;
    LayoutInflater layoutInflater;
    List<FileGroup> files = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_file_acitivity);

        downloadListView = findViewById(R.id.download_list_view);
        downloadListView.setLayoutManager(new LinearLayoutManager(this));
        layoutInflater = LayoutInflater.from(this);

        files = packageFileGroup(this);


        GroupRecyclerAdapter<FileGroup, FileGroupViewHolder, FileViewHolder> recyclerAdapter =
                new GroupRecyclerAdapter<FileGroup, FileGroupViewHolder, FileViewHolder>(files) {
                    @Override
                    protected FileGroupViewHolder onCreateGroupViewHolder(ViewGroup parent) {
                        return new FileGroupViewHolder(layoutInflater.inflate(R.layout.download_list_group_layout, parent, false));
                    }

                    @Override
                    protected FileViewHolder onCreateChildViewHolder(ViewGroup parent) {
                        return new FileViewHolder(DownloadFileActivity.this, layoutInflater.inflate(R.layout.download_list_child_layout, parent, false));
                    }

                    @Override
                    protected void onBindGroupViewHolder(FileGroupViewHolder holder, int groupPosition) {
                        holder.update(getGroup(groupPosition));
                    }

                    @Override
                    protected void onBindChildViewHolder(FileViewHolder holder, int groupPosition, int childPosition) {
                        holder.update(getGroup(groupPosition).getMembers().get(childPosition));
                    }

                    @Override
                    protected int getChildCount(FileGroup group) {
                        return group.getMembers().size();
                    }
                };
        downloadListView.setAdapter(recyclerAdapter);
    }

    public void onBack(View view){
        finish();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Member member = files.get(groupPosition).getMembers().get(childPosition);
        Intent intent = new Intent();
        File file = new File(DOWNLOAD_PATH + File.separator + member.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置标记
        intent.setAction(Intent.ACTION_VIEW);//动作，查看

        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(this, "com.liuxiaoer.webview.fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, FileUtil.getMIMEType(file));
        } else {
            intent.setDataAndType(Uri.fromFile(file), FileUtil.getMIMEType(file));//设置类型
        }
        startActivity(intent);
        return true;
    }
}


class FileGroupViewHolder extends RecyclerView.ViewHolder {
    TextView name;

    public FileGroupViewHolder(@NonNull View itemView) {
        super(itemView);
        this.name = itemView.findViewById(R.id.download_list_group);
    }

    public void update(FileGroup group) {
        name.setText(group.getName());
    }
}

class FileViewHolder extends RecyclerView.ViewHolder {
    private Activity activity;
    ImageView imageView;
    TextView name;
    TextView size;

    public FileViewHolder(Activity activity, @NonNull View itemView) {
        super(itemView);
        this.activity = activity;
        imageView = itemView.findViewById(R.id.file_type_img);
        name = itemView.findViewById(R.id.file_name);
        size = itemView.findViewById(R.id.file_size);
    }

    public void update(Member member) {
        int type = member.getType();
        if (type == 0) {
            type = R.drawable.common_google_signin_btn_icon_light;
        }
        imageView.setImageDrawable(activity.getResources().getDrawable(type));
        name.setText(member.getName());
        size.setText(member.getSize());
    }
}