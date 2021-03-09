/*
 * 增加运行时变量相关功能，便于业务系统工作流执行过程中共享全局及局部变量数据
 */
-- 1. 增加运行时变量表
-- 1.1. 增加运行时变量表ID序列
CREATE SEQUENCE "public"."t_ds_runtime_variable_id_sequence"
    INCREMENT 1
    MINVALUE  1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1;
SELECT setval('"public"."t_ds_runtime_variable_id_sequence"', 1, FALSE);
ALTER SEQUENCE "public"."t_ds_runtime_variable_id_sequence" OWNER TO "postgres";
-- 1.2. 增加运行时变量表
CREATE TABLE "public"."t_ds_runtime_variable"
    (
        "id" int4 NOT NULL DEFAULT nextval('t_ds_runtime_variable_id_sequence'::regclass),
        "process_instance_id" int4,
        "task_instance_id" int4,
        "var_name" VARCHAR(32) COLLATE "pg_catalog"."default",
        "var_value" text COLLATE "pg_catalog"."default",
        CONSTRAINT "t_ds_runtime_variable_pkey" PRIMARY KEY ( "id" )
    );
ALTER TABLE "public"."t_ds_runtime_variable"
    OWNER TO "ds_user";
COMMENT
ON COLUMN "public"."t_ds_runtime_variable"."id" IS '生成ID';
COMMENT
ON COLUMN "public"."t_ds_runtime_variable"."process_instance_id" IS '关联流程实例';
COMMENT
ON COLUMN "public"."t_ds_runtime_variable"."task_instance_id" IS '关联任务实例';
COMMENT
ON COLUMN "public"."t_ds_runtime_variable"."var_name" IS '变量名';
COMMENT
ON COLUMN "public"."t_ds_runtime_variable"."var_value" IS '变量值';
COMMENT
ON TABLE "public"."t_ds_runtime_variable" IS '运行时变量表';