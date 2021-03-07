/*
 * 增加流程模板及相关功能，便于与业务系统接驳配置业务参数
 */
-- 1. 增加业务类型表
-- 1.1. 增加业务类型表ID序列
CREATE SEQUENCE "public"."t_ds_biz_type_id_sequence"
    INCREMENT 1
    MINVALUE  1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
SELECT setval('"public"."t_ds_biz_type_id_sequence"', 1, FALSE);
ALTER SEQUENCE "public"."t_ds_biz_type_id_sequence" OWNER TO "postgres";
-- 1.2. 增加业务类型表
CREATE TABLE "public"."t_ds_biz_type"
    (
        "id" int4 NOT NULL DEFAULT nextval('t_ds_biz_type_id_sequence'::regclass),
        "parent_id" int4,
        "type_code" VARCHAR(32),
        "type_name" VARCHAR(64),
        PRIMARY KEY ( "id" )
    );
COMMENT
ON COLUMN "public"."t_ds_biz_type"."id" IS '生成ID';
COMMENT
ON COLUMN "public"."t_ds_biz_type"."parent_id" IS '父ID';
COMMENT
ON COLUMN "public"."t_ds_biz_type"."type_code" IS '类型编码';
COMMENT
ON COLUMN "public"."t_ds_biz_type"."type_name" IS '类型名称';
COMMENT
ON TABLE "public"."t_ds_biz_type" IS '业务类型表';
-- 2. 增加项目业务类型关联表
-- 2.1. 增加项目业务类型关联表ID序列
CREATE SEQUENCE "public"."t_ds_relation_project_biz_type_id_sequence"
    INCREMENT 1
    MINVALUE  1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
SELECT setval('"public"."t_ds_relation_project_biz_type_id_sequence"', 1, FALSE);
ALTER SEQUENCE "public"."t_ds_relation_project_biz_type_id_sequence" OWNER TO "postgres";
-- 2.2. 增加项目业务类型关联表
CREATE TABLE "public"."t_ds_relation_project_biz_type"
    (
        "id" int4 NOT NULL DEFAULT nextval('t_ds_relation_project_biz_type_id_sequence'::regclass),
        "project_id" int4,
        "biz_type_id" int4,
        "create_time" TIMESTAMP(6) DEFAULT NULL:: TIMESTAMP WITHOUT TIME ZONE,
        "update_time" TIMESTAMP(6) DEFAULT NULL:: TIMESTAMP WITHOUT TIME ZONE,
        CONSTRAINT "t_ds_relation_project_biz_type_pkey" PRIMARY KEY ( "id" )
    );
ALTER TABLE "public"."t_ds_relation_project_biz_type"
    OWNER TO "postgres";
COMMENT
ON TABLE "public"."t_ds_relation_project_biz_type" IS '项目业务类型关联表';
-- 3. 增加流程模板表
-- 3.1. 增加流程模板表ID序列
CREATE SEQUENCE "public"."t_ds_process_template_id_sequence"
    INCREMENT 1
    MINVALUE  1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
SELECT setval('"public"."t_ds_process_template_id_sequence"', 1, FALSE);
ALTER SEQUENCE "public"."t_ds_process_template_id_sequence" OWNER TO "postgres";
-- 3.2. 增加流程模板表
CREATE TABLE "public"."t_ds_process_template"
    (
        "id" int4 NOT NULL DEFAULT nextval('t_ds_process_template_id_sequence'::regclass),
        "name" VARCHAR(255) COLLATE "pg_catalog"."default" DEFAULT NULL:: CHARACTER VARYING,
        "biz_type_id" int4,
        "biz_form_url" VARCHAR(255) COLLATE "pg_catalog"."default" DEFAULT NULL:: CHARACTER VARYING,
        "project_id" int4,
        "user_id" int4,
        "version" int4,
        "release_state" int4,
        "flag" int4,
        "create_time" TIMESTAMP(6),
        "update_time" TIMESTAMP(6),
        "modify_by" VARCHAR(36) DEFAULT '',
        "description" text COLLATE "pg_catalog"."default",
        "locations" text COLLATE "pg_catalog"."default",
        "connects" text COLLATE "pg_catalog"."default",
        "process_template_json" text COLLATE "pg_catalog"."default",
        "global_params" text COLLATE "pg_catalog"."default",
        "resource_ids" VARCHAR(255) DEFAULT NULL,
        "tenant_id" int4 NOT NULL DEFAULT ( -1 ),
        CONSTRAINT "t_ds_process_template_pkey" PRIMARY KEY ( "id" )
    );
ALTER TABLE "public"."t_ds_process_template"
    OWNER TO "postgres";
CREATE
INDEX "process_template_index" ON "public"."t_ds_process_template" USING btree (
  "project_id" "pg_catalog"."int4_ops" ASC NULLS LAST,
  "id" "pg_catalog"."int4_ops" ASC NULLS LAST
);
COMMENT
ON COLUMN "public"."t_ds_process_template"."biz_type_id" IS '关联业务类型';
COMMENT
ON COLUMN "public"."t_ds_process_template"."biz_form_url" IS '业务表单链接';
COMMENT
ON TABLE "public"."t_ds_process_template" IS '流程模板表';
-- 4. 修改流程定义表，增加“关联流程模板”字段
ALTER TABLE "public"."t_ds_process_definition"
    ADD COLUMN "biz_type_id" int4,
    ADD COLUMN "biz_form_url" VARCHAR(255) COLLATE "pg_catalog"."default" DEFAULT NULL::CHARACTER VARYING;
ADD COLUMN "template_id" int4;
COMMENT
ON COLUMN "public"."t_ds_process_definition"."biz_type_id" IS '关联业务类型';
COMMENT
ON COLUMN "public"."t_ds_process_definition"."biz_form_url" IS '业务表单链接';
COMMENT
ON COLUMN "public"."t_ds_process_definition"."template_id" IS '关联流程模板';
-- 5. 修改项目表，增加“应用根地址”字段
ALTER TABLE "public"."t_ds_project"
    ADD COLUMN "app_root_url" VARCHAR(255);
COMMENT
ON COLUMN "public"."t_ds_project"."app_root_url" IS '应用根路径';