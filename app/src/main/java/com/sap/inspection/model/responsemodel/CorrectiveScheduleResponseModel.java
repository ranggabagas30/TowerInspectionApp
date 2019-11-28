package com.sap.inspection.model.responsemodel;

import java.util.ArrayList;

public class CorrectiveScheduleResponseModel extends BaseResponseModel {

    private ArrayList<CorrectiveSchedule> data;

    private long updated_at;

    public ArrayList<CorrectiveSchedule> getData() {
        return data;
    }

    public void setData(ArrayList<CorrectiveSchedule> data) {
        this.data = data;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public static class CorrectiveSchedule {

        private Integer id;

        private ArrayList<CorrectiveGroup> group = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public ArrayList<CorrectiveGroup> getGroup() {
            return group;
        }

        public void setGroup(ArrayList<CorrectiveGroup> group) {
            this.group = group;
        }
    }

    public static class CorrectiveGroup {

        private Integer id;

        private ArrayList<CorrectiveItem> items = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public ArrayList<CorrectiveItem> getItems() {
            return items;
        }

        public void setItems(ArrayList<CorrectiveItem> items) {
            this.items = items;
        }
    }

    public static class CorrectiveItem {

        private Integer id;

        private String scope_type;

        private Integer row_id;

        private Integer row_column_id;

        private Integer column_id;

        private String ancestry;

        private ArrayList<Integer> operator = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getScopeType() {
            return scope_type;
        }

        public void setScopeType(String scopeType) {
            this.scope_type = scopeType;
        }

        public Integer getRow_id() {
            return row_id;
        }

        public void setRow_id(Integer row_id) {
            this.row_id = row_id;
        }

        public Integer getRow_column_id() {
            return row_column_id;
        }

        public void setRow_column_id(Integer row_column_id) {
            this.row_column_id = row_column_id;
        }

        public Integer getColumn_id() {
            return column_id;
        }

        public void setColumn_id(Integer column_id) {
            this.column_id = column_id;
        }

        public String getAncestry() {
            return ancestry;
        }

        public void setAncestry(String ancestry) {
            this.ancestry = ancestry;
        }

        public ArrayList<Integer> getOperator() {
            return operator;
        }

        public void setOperator(ArrayList<Integer> operator) {
            this.operator = operator;
        }
    }
}
