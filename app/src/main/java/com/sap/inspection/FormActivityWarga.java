package com.sap.inspection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CheckApprovalResponseModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.DeleteWargaAndBarangDialog;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.MyTextView;
import com.sap.inspection.views.adapter.NavigationAdapter;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Predicate;

public class FormActivityWarga extends BaseActivity {

    private MyTextView mHeaderTitle, mHeaderSubtitle;
    private RecyclerView mNavigationMenu;
    private LovelyTextInputDialog mInputJumlahBarangDialog;
    private DeleteWargaAndBarangDialog mDeleteBarangDialog;
    private RecyclerNavigationAdapter mNavigationAdapter;
    private Vector<RowModel> mNavigationItemsParentOnly = new Vector<>();
    private Vector<RowModel> mNavigationItems = new Vector<>();

    private final int NAVIGATION_ITEM_PARENT = 0;
    private final int NAVIGATION_ITEM_CHILD  = 1;

    private boolean expand      = true;
    private boolean collapse    = false;

    // bundle extras
    private int dataIndex;
    private int rowId;
    private String scheduleId;
    private String wargaId;
    private String barangId;
    private String workFormGroupId;
    private String workFormGroupName;
    private String workFormParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_activity_warga);

        DebugLog.d("received bundles : ");
        dataIndex           = getIntent().getIntExtra(Constants.KEY_DATAINDEX, -1);
        rowId               = getIntent().getIntExtra(Constants.KEY_ROWID, -1);
        scheduleId          = getIntent().getStringExtra(Constants.KEY_SCHEDULEID); DebugLog.d("scheduleId = " + scheduleId);
        wargaId             = getIntent().getStringExtra(Constants.KEY_WARGAID);    DebugLog.d("wargaId = " + wargaId);
        workFormGroupId     = getIntent().getStringExtra(Constants.KEY_WORKFORMGROUPID); DebugLog.d("workFormGroupId = " + workFormGroupId);
        workFormGroupName   = getIntent().getStringExtra(Constants.KEY_WORKFORMGROUPNAME); DebugLog.d("workFormGroupName = " + workFormGroupName);
        workFormParentId    = getIntent().getStringExtra(Constants.KEY_PARENTID); DebugLog.d("parentId = " + workFormParentId);

        mHeaderTitle = findViewById(R.id.header_title);
        mHeaderSubtitle = findViewById(R.id.header_subtitle);
        mNavigationMenu = findViewById(R.id.recyclerviewNavigation);

        mInputJumlahBarangDialog = new LovelyTextInputDialog(this, R.style.CheckBoxTintTheme)
                .setTopColorRes(R.color.item_drill_red)
                .setTopTitle("input Jumlah Barang")
                .setTopTitleColor(R.color.lightgray)
                .setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);

        mNavigationAdapter = new RecyclerNavigationAdapter();
        mNavigationMenu.setAdapter(mNavigationAdapter);
        mNavigationMenu.setItemAnimator(new DefaultItemAnimator());
        mNavigationMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        generateNavigationItems(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DebugLog.d("onResume");

        int work_form_group_id = Integer.valueOf(workFormGroupId);
        String wargaLable = "Warga ID ";
        String wargaID = StringUtil.getRegisteredWargaId(scheduleId, wargaId);
        String wargaName = StringUtil.getName(scheduleId, wargaId, Constants.EMPTY, work_form_group_id);

        StringBuilder wargaLableBuilder = new StringBuilder(wargaLable).append(wargaID);

        if (!TextUtils.isEmpty(wargaName))
            wargaLableBuilder.append( " (").append(wargaName).append(")");

        mHeaderTitle.setText(new String(wargaLableBuilder));
        mHeaderSubtitle.setText("Schedule ID " + scheduleId);
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setWargaId(String wargaId) {
        this.wargaId = wargaId;
    }

    public String getWargaId() {
        return wargaId;
    }

    public void setBarangId(String barangId) {
        this.barangId = barangId;
    }

    public String getBarangId() {
        return barangId;
    }

    public void generateNavigationItems(boolean isChanging) {

        // generate navigation menu based on parentId of wargaKe
        if (isChanging) {
            mNavigationItems = mNavigationItemsParentOnly = RowModel.getWargaKeNavigationItemsRowModel(workFormParentId, scheduleId, wargaId);
        } else {
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

        mNavigationAdapter.setItems(mNavigationItems);
    }

    public Vector<RowModel> generateNavigationItemsChanged() {

        DebugLog.d("generate navigation items changed");
        Vector<RowModel> newNavigationItems = new Vector<>();

        for (RowModel navigationItem : mNavigationItemsParentOnly) {
            newNavigationItems.add(navigationItem);
            if (navigationItem.isOpen && hasChild(navigationItem)) {
                newNavigationItems.addAll(navigationItem.children);
            }
        }

        return newNavigationItems;
    }

    public void removeNavigationItem(RowModel removeItem) {

        DebugLog.d("==== remove item with label : " + removeItem.text);

        for (RowModel navigationItem : mNavigationItemsParentOnly) {
            if (navigationItem.isOpen && hasChild(navigationItem)) {
                DebugLog.d("success removing ? " + navigationItem.children.remove(removeItem));
            }
        }

        generateNavigationItems(false);
    }

    public void toggleExpand(RowModel navigationItem) {

        if (navigationItem.isOpen){
            navigationItem.isOpen = false;
            DebugLog.d("closed");
        }
        else if (hasChild(navigationItem)){
            navigationItem.isOpen = true;
            DebugLog.d("open");
        }else{
            DebugLog.d("not open");
        }

        generateNavigationItems(false);
    }

    public boolean hasChild(RowModel navigationItem) {
        return navigationItem.children != null && !navigationItem.children.isEmpty();
    }

    private void navigateToFormFillActivity(RowModel rowModel) {

        String realWargaId = StringUtil.getRegisteredWargaId(scheduleId, getWargaId());

        setBarangId(Constants.EMPTY);
        if (rowModel.text.contains(Constants.regexId)) {
            setBarangId(StringUtil.getIdFromLabel(rowModel.text));
        }

        String realBarangId = StringUtil.getRegisteredBarangId(scheduleId, realWargaId, getBarangId());

        DebugLog.d("(realwargaid, realbarangid) : (" + realWargaId + ", " + realBarangId + ")");

        if (!TextUtils.isEmpty(realWargaId)) {
            if (StringUtil.isNotRegistered(realWargaId) && !rowModel.text.equalsIgnoreCase("Informasi Diri")) {

                MyApplication.getInstance().toast("Tidak bisa melanjutkan membuka form. Silahkan upload data 'Informasi Diri' terlebih dahulu", Toast.LENGTH_LONG);

            } else if (rowModel.text.equalsIgnoreCase("Kwitansi")) {

                proceedApprovalCheckingFirst(scheduleId, workFormGroupName, rowModel.id, rowModel.work_form_group_id, realWargaId, realBarangId);

            } else{

                BaseActivity.navigateToFormFillActivity(
                        scheduleId,
                        rowModel.id,
                        rowModel.work_form_group_id,
                        workFormGroupName,
                        null,
                        realWargaId,
                        realBarangId
                );

            }
        }

    }

    private void proceedApprovalCheckingFirst(String scheduleId, String workFormGroupName, int rowId, int workFormGroupId, String wargaId, String barangId) {

        DebugLog.d("proceed approval checking ... ");
        CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(this, scheduleId, workFormGroupName, rowId, workFormGroupId, wargaId, barangId);
        APIHelper.getCheckApproval(this, checkApprovalHandler, scheduleId);

    }

    private class CheckApprovalHandler extends Handler {

        private Context context;
        private String scheduleId;
        private String workFormGroupName;
        private String wargaId;
        private String barangId;
        private int rowId;
        private int workFormGroupId;

        public CheckApprovalHandler(Context context, String scheduleId, String workFormGroupName, int rowId, int workFormGroupId, String wargaId, String barangId) {
            this.context = context;
            this.scheduleId = scheduleId;
            this.workFormGroupName = workFormGroupName;
            this.rowId = rowId;
            this.workFormGroupId = workFormGroupId;
            this.wargaId = wargaId;
            this.barangId = barangId;
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");
            Gson gson = new Gson();

            if (isResponseOK) {

                if (bundle.getString("json") != null){

                    CheckApprovalResponseModel checkApprovalResponseModel = gson.fromJson(bundle.getString("json"), CheckApprovalResponseModel.class);
                    checkApprovalResponseModel.toString();

                    if (!checkApprovalResponseModel.messages.equalsIgnoreCase("failed")) {

                        DebugLog.d("check approval success");

                        FormImbasPetirConfig.setScheduleApproval(scheduleId, true);

                        BaseActivity.navigateToFormFillActivity(
                                scheduleId,
                                rowId,
                                workFormGroupId,
                                workFormGroupName,
                                null,
                                wargaId,
                                barangId
                        );

                        return;
                    }

                    DebugLog.d("belum ada approval dari STP");
                    MyApplication.getInstance().toast("Schedule menunggu approval dari STP", Toast.LENGTH_LONG);

                } else {

                    MyApplication.getInstance().toast("Gagal mengecek approval. Response json = null", Toast.LENGTH_LONG);

                }
            } else {

                MyApplication.getInstance().toast("Gagal mengecek approval. Response not OK dari server", Toast.LENGTH_LONG);
                DebugLog.d("response not ok");
            }
        }
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

            DebugLog.d("row (position, name) : (" + position + ", " + getItems().get(position).text + ")");

            if (holder instanceof ParentViewHolder) {

                ((ParentViewHolder) holder).bindLayout(getItems().get(position));
                ((ParentViewHolder) holder).bindAdapter(getItems().get(holder.getAdapterPosition()));

            } else if (holder instanceof ChildViewHolder) {

                ((ChildViewHolder) holder).bindLayout(getItems().get(position));
                ((ChildViewHolder) holder).bindAdapter(getItems().get(holder.getAdapterPosition()));

            }
        }

        @Override
        public int getItemCount() {

            return getItems().size();
        }

        @Override
        public int getItemViewType(int position) {

            int viewType = NAVIGATION_ITEM_PARENT;

            if (getItems().get(position).parent_id != Integer.valueOf(workFormParentId)) {

                viewType = NAVIGATION_ITEM_CHILD;

            }

            DebugLog.d("view type : " + viewType);
            return viewType;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setItems(Vector<RowModel> navigationItems) {
            this.navigationItems = navigationItems;
            notifyDataSetChanged();
        }

        public Vector<RowModel> getItems() {
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

            public void bindLayout(RowModel parentNavigationItem) {
                title.setText(parentNavigationItem.text);
                uploadItems.setVisibility(View.VISIBLE);

                if (parentNavigationItem.text.equalsIgnoreCase("Informasi Barang"))
                    uploadItems.setVisibility(View.INVISIBLE);
            }

            public void bindAdapter(RowModel parentNavigationItem) {
                title.setTag(parentNavigationItem);
                uploadItems.setTag(parentNavigationItem);
            }

            @Override
            public void onClick(View v) {

                int id = v.getId();

                RowModel itemClick = (RowModel) v.getTag();

                switch (id) {

                    case R.id.workformgroup_upload :
                        //MyApplication.getInstance().toast("Upload data", Toast.LENGTH_SHORT);
                        uploadPerWargaSubNavigationMenu(itemClick);
                        break;
                    case R.id.expandCollapse :
                        //MyApplication.getInstance().toast("expand", Toast.LENGTH_SHORT);
                        break;
                    case R.id.title :
                        //MyApplication.getInstance().toast("title click", Toast.LENGTH_SHORT);

                        RowModel parentNavigationItem = (RowModel) v.getTag();

                        if (!hasChild(parentNavigationItem)) {

                            navigateToFormFillActivity(parentNavigationItem);

                        } else {

                            toggleExpand(parentNavigationItem);
                        }
                        break;
                }
            }

            private void uploadPerWargaSubNavigationMenu(RowModel parentNavItem) {

                String scheduleId   = getScheduleId();
                String realWargaId  = StringUtil.getRegisteredWargaId(scheduleId, getWargaId());
                int workFormGroupId = parentNavItem.work_form_group_id;

                DebugLog.d("parent item click. upload items by (scheduleid, wargaid, workformgroupid) : (" + scheduleId + ", " + workFormGroupName + ", " + realWargaId + ")");
                /*ArrayList<ItemValueModel> itemUploads = ItemValueModel.getItemValuesForUpload(scheduleId, workFormGroupId, realWargaId, null);
                ItemUploadManager.getInstance().addItemValues(itemUploads);*/

                new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, realWargaId, Constants.EMPTY).execute();
            }
        }

        public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

                title.setOnClickListener(this);
                uploadItems.setOnClickListener(this);
                removeSubMenu.setOnClickListener(this);
            }

            public void bindLayout(RowModel childNavigationItem) {
                title.setText(childNavigationItem.text);

                removeSubMenu.setVisibility(View.INVISIBLE);
                uploadItems.setVisibility(View.INVISIBLE);

                if (childNavigationItem.text.contains(Constants.regexId)) {
                    removeSubMenu.setVisibility(View.VISIBLE);
                    uploadItems.setVisibility(View.VISIBLE);
                }
            }

            public void bindAdapter(RowModel childNavigationItem) {
                title.setTag(childNavigationItem);
                uploadItems.setTag(childNavigationItem);
                removeSubMenu.setTag(childNavigationItem);
            }

            @Override
            public void onClick(View v) {

                DebugLog.d("children item clicked");

                RowModel itemClick = (RowModel) v.getTag();

                String labelMenu = itemClick.text;

                int id = v.getId();

                switch (id) {

                    case R.id.title :

                        if (labelMenu.equalsIgnoreCase("Tambah barang")) {

                            showInputAmountBarangDialog();

                        } else if (labelMenu.contains(Constants.regexId)) {

                            navigateToFormFillActivity(itemClick);

                        }
                        break;
                    case R.id.removesubmenu :

                        if (labelMenu.contains(Constants.regexId)) {

                            showConfirmDeleteBarangDialog(itemClick);
                        }
                        break;
                    case R.id.workformgroup_upload :

                        if (labelMenu.contains(Constants.regexId)) {

                            uploadItemsByBarangId(itemClick);

                        }
                        break;

                }
            }

            public void showInputAmountBarangDialog() {

                mInputJumlahBarangDialog.setConfirmButton("Tambah", amountOfBarang -> {

                    // insert new data warga as many as amount inputted
                    MyApplication.getInstance().toast("Tambahan jumlah barang : " + amountOfBarang, Toast.LENGTH_LONG);

                    String realWargaId  = StringUtil.getRegisteredWargaId(scheduleId, getWargaId());
                    FormImbasPetirConfig.insertDataBarang(dataIndex, realWargaId, Integer.valueOf(amountOfBarang));
                    generateNavigationItems(true);

                }).show();
            }

            public void showConfirmDeleteBarangDialog(RowModel removedBarangItem) {

                mDeleteBarangDialog = new DeleteWargaAndBarangDialog(FormActivityWarga.this, removedBarangItem);
                mDeleteBarangDialog.setOnPositiveClickListener(this::removeBarangId);
                mDeleteBarangDialog.show();
            }

            public void removeBarangId(RowModel removedChildItem) {

                String realWargaId  = StringUtil.getRegisteredWargaId(scheduleId, getWargaId());

                setBarangId(StringUtil.getIdFromLabel(removedChildItem.text));
                String realBarangId = StringUtil.getRegisteredBarangId(scheduleId, realWargaId, getBarangId());

                DebugLog.d("remove barang with (id, label, wargaid, barangid) : (" + removedChildItem.id + ", " + removedChildItem.text + ", " + realWargaId + ", " + realBarangId + ")");

                APIHelper.deleteBarang(FormActivityWarga.this, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        String successfulMessage = "Sukses hapus data barangId (" + realBarangId + ")";
                        String failedMessage	 = "Gagal hapus data barangId (" + realBarangId + "). Item telah terhapus atau tidak ditemukan";

                        Bundle bundle = msg.getData();
                        Gson gson = new Gson();

                        BaseResponseModel responseDeleteBarangModel = gson.fromJson(bundle.getString("json"), BaseResponseModel.class);

                        boolean isResponseOK = bundle.getBoolean("isresponseok");

                        if (isResponseOK) {

                            ItemValueModel.deleteAllBy(scheduleId, realWargaId, realBarangId);

                            boolean isSuccessful = FormImbasPetirConfig.removeBarang(scheduleId, realWargaId, realBarangId);
                            if (isSuccessful) {

                                DebugLog.d("remove barangid berhasil dengan message : " + responseDeleteBarangModel.messages);
                                removeNavigationItem(removedChildItem);
                                MyApplication.getInstance().toast(successfulMessage, Toast.LENGTH_LONG);
                            } else {
                                MyApplication.getInstance().toast(failedMessage, Toast.LENGTH_LONG);
                            }

                        } else {

                            MyApplication.getInstance().toast(failedMessage + ". Repsonse not OK from server", Toast.LENGTH_LONG);
                            DebugLog.e("response not ok");
                        }
                    }
                }, realBarangId);
            }

            public void uploadItemsByBarangId(RowModel uploadChildItem) {

                String scheduleId = getScheduleId();
                int workFormGroupId = uploadChildItem.work_form_group_id;

                String realWargaId  = StringUtil.getRegisteredWargaId(scheduleId, getWargaId());

                setBarangId(StringUtil.getIdFromLabel(uploadChildItem.text));
                String realBarangId = StringUtil.getRegisteredBarangId(scheduleId, realWargaId, getBarangId());

                DebugLog.d("(real wargaid, real barangid) : (" + realWargaId + ", " + realBarangId + ")");

                new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, realWargaId, realBarangId).execute();
            }
        }
    }


}
