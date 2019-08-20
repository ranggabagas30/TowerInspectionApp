package com.sap.inspection.model.responsemodel;

import java.util.Vector;

public class CorrectiveScheduleResponseModel extends BaseResponseModel {

    private Vector<CorrectiveSchedule> data;

    private long updated_at;

    public Vector<CorrectiveSchedule> getData() {
        return data;
    }

    public void setData(Vector<CorrectiveSchedule> data) {
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

        private Vector<CorrectiveGroup> group = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Vector<CorrectiveGroup> getGroup() {
            return group;
        }

        public void setGroup(Vector<CorrectiveGroup> group) {
            this.group = group;
        }
    }

    public static class CorrectiveGroup {

        private Integer id;

        private Vector<CorrectiveItem> items = null;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Vector<CorrectiveItem> getItems() {
            return items;
        }

        public void setItems(Vector<CorrectiveItem> items) {
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

        private Vector<Integer> operator = null;

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

        public Vector<Integer> getOperator() {
            return operator;
        }

        public void setOperator(Vector<Integer> operator) {
            this.operator = operator;
        }
    }
}
