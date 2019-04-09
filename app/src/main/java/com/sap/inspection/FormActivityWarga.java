package com.sap.inspection;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.BaseActivity;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.MyTextView;

import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Predicate;

public class FormActivityWarga extends BaseActivity {

    private MyTextView mHeaderTitle, mHeaderSubtitle;
    private RecyclerView mNavigationMenu;

    private RecyclerNavigationAdapter mNavigationAdapter;
    private Vector<RowModel> mNavigationItemsParentOnly = new Vector<>();
    private Vector<RowModel> mNavigationItems = new Vector<>();

    private final int NAVIGATION_ITEM_PARENT = 0;
    private final int NAVIGATION_ITEM_CHILD  = 1;

    private boolean expand      = true;
    private boolean collapse    = false;

    // bundle extras
    private String scheduleId;
    private String wargaId;
    private String workFormGroupId;
    private String workFormGroupName;
    private String workFormParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_activity_warga);

        DebugLog.d("received bundles : ");
        scheduleId          = getIntent().getStringExtra(Constants.KEY_SCHEDULEID); DebugLog.d("scheduleId = " + scheduleId);
        wargaId             = getIntent().getStringExtra(Constants.KEY_WARGAID);    DebugLog.d("wargaId = " + wargaId);
        workFormGroupId     = getIntent().getStringExtra(Constants.KEY_WORKFORMGROUPID); DebugLog.d("workFormGroupId = " + workFormGroupId);
        workFormGroupName   = getIntent().getStringExtra(Constants.KEY_WORKFORMGROUPNAME); DebugLog.d("workFormGroupName = " + workFormGroupName);
        workFormParentId    = getIntent().getStringExtra(Constants.KEY_PARENTID); DebugLog.d("parentId = " + workFormParentId);

        mHeaderTitle = findViewById(R.id.header_title);
        mHeaderSubtitle = findViewById(R.id.header_subtitle);
        mNavigationMenu = findViewById(R.id.recyclerviewNavigation);

        mHeaderTitle.setText("Warga ID " + wargaId);
        mHeaderSubtitle.setText("Schedule ID " + scheduleId);

        mNavigationAdapter = new RecyclerNavigationAdapter();
        mNavigationMenu.setAdapter(mNavigationAdapter);
        mNavigationMenu.setItemAnimator(new DefaultItemAnimator());
        mNavigationMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        generateNavigationItems();
    }

    public void generateNavigationItems() {

        // generate navigation menu based on parentId of wargaKe
        if (mNavigationItems.isEmpty()) {
            mNavigationItems = mNavigationItemsParentOnly = RowModel.getWargaKeNavigationItemsRowModel(workFormParentId);
        }
        else {
            mNavigationItems = generateNavigationItemsChanged();
        }

        if (mNavigationItems != null) {

            DebugLog.d("shown size = "+mNavigationItems.size());
            int position = 0;
            for (RowModel rowModel : mNavigationItems) {
                DebugLog.d("id : " + rowModel.id);
                DebugLog.d("name : " + rowModel.text);
                DebugLog.d("ancestry name : " + mNavigationItems.get(position).text);
                DebugLog.d("ancestry : " + rowModel.ancestry);
                DebugLog.d("parentId : " + rowModel.parent_id);
                DebugLog.d("level : " + rowModel.level);
                DebugLog.d("hasForm : " + rowModel.hasForm);
                DebugLog.d("workFormGroupId : " + rowModel.work_form_group_id);
                if (rowModel.children != null) {
                    DebugLog.d("children size : " + rowModel.children.size());
                    for (RowModel child : rowModel.children) {
                        DebugLog.d("--- child id : " + child.id);
                        DebugLog.d("--- child workFormGroupId : " + child.work_form_group_id);
                        DebugLog.d("--- child name : " + child.text);
                        DebugLog.d("--- child ancestry : " + child.ancestry);
                        DebugLog.d("--- child parentId : " + child.parent_id);
                        DebugLog.d("--- child level : " + child.level);
                        DebugLog.d("--- child hasForm : " + child.hasForm);
                        DebugLog.d("--- child workFormGroupId : " + child.work_form_group_id);
                    }
                }
                DebugLog.d("\n\n");
                position++;
            }
        }

        mNavigationAdapter.setNavigationItems(mNavigationItems);
    }

    public Vector<RowModel> generateNavigationItemsChanged() {

        DebugLog.d("generate navigation items changed");
        Vector<RowModel> newNavigationItems = new Vector<>();

        for (RowModel navigationItem : mNavigationItemsParentOnly) {
            newNavigationItems.add(navigationItem);
            if (navigationItem.isOpen && navigationItem.children != null && !navigationItem.children.isEmpty()) {
                newNavigationItems.addAll(navigationItem.children);
            }
        }

        return newNavigationItems;
    }

    public void toggleExpand(RowModel navigationItem) {

        if (navigationItem.isOpen){
            navigationItem.isOpen = false;
            DebugLog.d("closed");
        }
        else if (navigationItem.children != null && navigationItem.children.size() > 0){
            navigationItem.isOpen = true;
            DebugLog.d("open");
        }else{
            DebugLog.d("not open");
        }

        generateNavigationItems();
    }

    public boolean hasChild(RowModel navigationItem) {
        return navigationItem.children != null && !navigationItem.children.isEmpty();
    }

    private void navigateToFormFillActivity(RowModel rowModel) {

        Intent intent = new Intent(this, FormFillActivity.class);
        intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
        intent.putExtra(Constants.KEY_WARGAID, wargaId);
        intent.putExtra(Constants.KEY_ROWID, rowModel.id);
        intent.putExtra(Constants.KEY_WORKFORMGROUPID, rowModel.work_form_group_id);
        intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
        startActivity(intent);

    }

    private class RecyclerNavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Vector<RowModel> navigationItems = new Vector<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView;

            switch (viewType) {

                case NAVIGATION_ITEM_PARENT :
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation_1, parent, false);
                    return new ParentViewHolder(itemView);

                case NAVIGATION_ITEM_CHILD :
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation_2, parent, false);
                    return new ChildViewHolder(itemView);

                default:
                    DebugLog.d("item view type not found, set default parent item view type");
                    itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation_1, parent, false);
                    return new ParentViewHolder(itemView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            DebugLog.d("row (position, name) : (" + position + ", " + getNavigationItems().get(position).text + ")");

            if (holder instanceof ParentViewHolder) {

                ((ParentViewHolder) holder).bindLayout(getNavigationItems().get(position));
                ((ParentViewHolder) holder).bindAdapter(getNavigationItems().get(holder.getAdapterPosition()));

            } else if (holder instanceof ChildViewHolder) {

                ((ChildViewHolder) holder).bindLayout(getNavigationItems().get(position));
                ((ChildViewHolder) holder).bindAdapter(getNavigationItems().get(holder.getAdapterPosition()));

            }
        }

        @Override
        public int getItemCount() {

            int size = getNavigationItems().size();

            //DebugLog.d("amount of nav item(s) : " + size);
            return size;
        }

        @Override
        public int getItemViewType(int position) {

            int viewType = NAVIGATION_ITEM_PARENT;

            if (getNavigationItems().get(position).parent_id != Integer.valueOf(workFormParentId)) {

                viewType = NAVIGATION_ITEM_CHILD;

            }

            DebugLog.d("view type : " + viewType);
            return viewType;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setNavigationItems(Vector<RowModel> navigationItems) {
            this.navigationItems = navigationItems;
            notifyDataSetChanged();
        }

        public Vector<RowModel> getNavigationItems() {
            return navigationItems;
        }

        public class ParentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private ImageView expandCollapse;
            private ImageView uploadItems;
            private MyTextView title;

            public ParentViewHolder(View itemView) {
                super(itemView);
                expandCollapse  = itemView.findViewById(R.id.expandCollapse);
                uploadItems     = itemView.findViewById(R.id.workformgroup_upload);
                title           = itemView.findViewById(R.id.title);

                expandCollapse.setOnClickListener(this);
                uploadItems.setOnClickListener(this);
                title.setOnClickListener(this);
            }

            public void bindLayout(RowModel navigationItem) {
                title.setText(navigationItem.text);
            }

            public void bindAdapter(RowModel navigationItem) {
                title.setTag(navigationItem);
                uploadItems.setTag(navigationItem);
            }

            @Override
            public void onClick(View v) {

                int id = v.getId();

                switch (id) {

                    case R.id.workformgroup_upload :
                        MyApplication.getInstance().toast("Upload data", Toast.LENGTH_SHORT);
                        break;
                    case R.id.expandCollapse :
                        MyApplication.getInstance().toast("expand", Toast.LENGTH_SHORT);
                        break;
                    case R.id.title :
                        MyApplication.getInstance().toast("title click", Toast.LENGTH_SHORT);

                        RowModel navigationItem = (RowModel) v.getTag();

                        if (!hasChild(navigationItem)) {

                            navigateToFormFillActivity(navigationItem);

                        } else {

                            toggleExpand(navigationItem);
                        }
                        break;
                }
            }
        }

        public class ChildViewHolder extends RecyclerView.ViewHolder {

            private ImageView expandCollapse;
            private ImageView uploadItems;
            private ImageView removeSubMenu;
            private MyTextView title;

            public ChildViewHolder(View itemView) {
                super(itemView);
                expandCollapse  = itemView.findViewById(R.id.expandCollapse);
                uploadItems     = itemView.findViewById(R.id.workformgroup_upload);
                title           = itemView.findViewById(R.id.title);
                removeSubMenu   = itemView.findViewById(R.id.removesubmenu);
            }

            public void bindLayout(RowModel navigationItem) {
                title.setText(navigationItem.text);
            }

            public void bindAdapter(RowModel navigationItem) {
                uploadItems.setTag(navigationItem);
            }
        }
    }


}
