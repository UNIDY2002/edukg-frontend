package com.java.sunxun.models;

import java.util.ArrayList;

public class Linking {
    private final String context;

    private final ArrayList<LinkingResult> linkingResults;

    public Linking(String context) {
        this.context = context;
        this.linkingResults = new ArrayList<>();
    }

    public String getContext() {
        return context;
    }

    public ArrayList<LinkingResult> getLinkingResults() {
        return linkingResults;
    }

    public void link(String entityType, String entityUri, int startIndex, int endIndex, String entity) {
        this.linkingResults.add(new LinkingResult(entityType, entityUri, startIndex, endIndex, entity));
    }

    public static class LinkingResult {
        private final String entityType;  // 实体所属概念

        private final String entityUri;  // 实体uri

        private final int startIndex;  // 实体名称在文中出现位置的起点（包含）

        private final int endIndex;  // 实体名称在文中出现位置的终点（包含）

        private final String entity;  // 实体名称

        public LinkingResult(String entityType, String entityUri, int startIndex, int endIndex, String entity) {
            this.entityType = entityType;
            this.entityUri = entityUri;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.entity = entity;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getEntityUri() {
            return entityUri;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public String getEntity() {
            return entity;
        }
    }
}
