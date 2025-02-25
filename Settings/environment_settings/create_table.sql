-- 開発許可申請管理システムテーブル作成DDL
-- 順次実行しテーブルを作成してください。

-- O_問合せ宛先
drop table if exists o_inquiry_address cascade;

create table o_inquiry_address (
  inquiry_address_id integer not null
  , message_id integer
  , department_id character varying(10)
  , read_flag character(1)
  , answer_complete_flag character(1)
  , constraint o_inquiry_address_PKC primary key (inquiry_address_id)
) ;

-- M_道路判定ラベル
drop table if exists m_road_judge_label cascade;

create table m_road_judge_label (
  label_id integer not null
  , replace_identify text
  , index_value integer
  , index_text text
  , min_value double precision
  , max_value double precision
  , replace_text text
  , constraint m_road_judge_label_PKC primary key (label_id)
) ;

-- O_問合せファイル
drop table if exists o_inquiry_file cascade;

create table o_inquiry_file (
  inquiry_file_id integer not null
  , message_id integer
  , file_name varchar(255)
  , file_path text
  , register_datetime timestamp
  , constraint o_inquiry_file_PKC primary key (inquiry_file_id)
) ;

-- M_回答テンプレート
drop table if exists m_answer_template cascade;

create table m_answer_template (
  answer_template_id integer not null
  , disp_order integer
  , answer_template_text text
  , judgement_item_id character varying(10) not null
  , constraint m_answer_template_PKC primary key (answer_template_id)
) ;

create unique index idx_answer_template
  on m_answer_template(answer_template_id);

-- O_回答ファイル更新履歴
drop table if exists o_answer_file_history cascade;

create table o_answer_file_history (
  answer_file_history_id integer not null
  , answer_file_id integer
  , answer_id integer
  , update_type integer
  , update_user_id character varying(10)
  , update_datetime timestamp
  , notify_flag character(1)
  , constraint o_answer_file_history_PKC primary key (answer_file_history_id)
) ;

create unique index idx_o_answer_file_history
  on o_answer_file_history(answer_id,answer_file_id,answer_file_history_id);

-- O_回答履歴
drop table if exists o_answer_history cascade;

create table o_answer_history (
  answer_history_id integer not null
  , answer_id integer
  , answer_user_id character varying(10)
  , answer_datetime timestamp
  , answer_text text
  , notify_flag character(1)
  , discussion_item text
  , business_pass_status character(1)
  , business_pass_comment text
  , government_confirm_status character(1)
  , government_confirm_datetime timestamp
  , government_confirm_comment text
  , permission_judgement_result character(1)
  , re_application_flag character(1)
  , discussion_flag character(1)
  , answer_status character(1)
  , answer_data_type character(1)
  , update_datetime timestamp
  , deadline_datetime timestamp
  , constraint o_answer_history_PKC primary key (answer_history_id)
) ;

create unique index idx_o_nswer_history
  on o_answer_history(answer_id,answer_user_id,answer_history_id);

-- O_メッセージ
drop table if exists o_message cascade;

create table o_message (
  message_id integer not null
  , chat_id integer
  , message_type integer
  , sender_id character varying(10)
  , to_department_id character varying(10)
  , message_text text
  , send_datetime timestamp
  , read_flag character(1)
  , answer_complete_flag character(1)
  , constraint o_message_PKC primary key (message_id)
) ;

create unique index idx_o_message
  on o_message(chat_id,message_id);

-- O_チャット
drop table if exists o_chat cascade;

create table o_chat (
  chat_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , department_answer_id integer
  , answer_id integer
  , government_answer_datetime timestamp
  , establishment_first_post_datetime timestamp
  , last_answerer_id character varying(10)
  , establishment_post_datetime timestamp
  , constraint o_chat_PKC primary key (chat_id)
) ;

create unique index idx_o_chat
  on o_chat(application_id,application_step_id,department_answer_id,answer_id,chat_id);

-- M_行政ユーザ
drop table if exists m_government_user cascade;

create table m_government_user (
  user_id character varying(10) not null
  , login_id character varying(50) not null
  , password character varying(1024)
  , role_code character(1)
  , department_id character varying(10) not null
  , user_name character varying(100)
  , admin_flag character(1)
  , constraint m_government_user_PKC primary key (user_id)
) ;

-- M_申請情報検索結果
drop table if exists m_application_search_result cascade;

create table m_application_search_result (
  application_search_result_id character varying(10) not null
  , reference_type character(1)
  , display_column_name character varying(100)
  , display_order integer
  , table_name character varying(100)
  , table_column_name character varying(100)
  , response_key character varying(100)
  , table_width real
  , constraint m_application_search_result_PKC primary key (application_search_result_id)
) ;

-- M_地番検索結果定義
drop table if exists m_lot_number_search_result_definition cascade;

create table m_lot_number_search_result_definition (
  lot_number_search_definition_id character varying(10) not null
  , display_order integer
  , table_type character(1)
  , display_column_name character varying(50)
  , table_column_name character varying(100)
  , table_width real
  , response_key character varying(100)
) ;

-- O_申請区分
drop table if exists o_application_category cascade;

create table o_application_category (
  application_id integer not null
  , view_id character varying(10) not null
  , category_id character varying(10) not null
  , application_step_id integer not null
  , version_information integer not null
  , constraint o_application_category_PKC primary key (application_id,view_id,category_id,application_step_id,version_information)
) ;

create unique index idx_o_application_category
  on o_application_category(application_id,view_id,category_id,application_step_id,version_information);

-- O_回答ファイル
drop table if exists o_answer_file cascade;

create table o_answer_file (
  answer_file_id integer not null
  , answer_id integer
  , application_id integer not null
  , application_step_id integer not null
  , department_id character varying(10)
  , answer_file_name text
  , file_path text
  , notified_file_path text
  , delete_unnotified_flag character(1)
  , delete_flag character(1)
  , constraint o_answer_file_PKC primary key (answer_file_id)
) ;

alter table o_answer_file add constraint idx_o_answer_file
  unique (answer_id,answer_file_id) ;

-- O_申請ファイル
drop table if exists o_application_file cascade;

create table o_application_file (
  file_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , application_file_id character varying(10) not null
  , upload_file_name text
  , file_path text
  , extension character varying(10)
  , version_information integer
  , upload_datetime timestamp
  , direction_department text
  , revise_content text
  , delete_flag character(1) default 0
  , constraint o_application_file_PKC primary key (file_id)
) ;

create unique index idx_o_application_file
  on o_application_file(application_id,application_step_id,application_file_id,version_information,file_id);

-- O_回答
drop table if exists o_answer cascade;

create table o_answer (
  answer_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , judgement_id character varying(10) not null
  , department_answer_id integer
  , department_id character varying(10)
  , judgement_result text
  , judgement_result_index integer default 0
  , answer_content text
  , notified_text text
  , register_datetime timestamp
  , update_datetime timestamp
  , complete_flag character(1)
  , notified_flag character(1)
  , answer_update_flag character(1)
  , re_application_flag character(1)
  , business_reapplication_flag character(1)
  , discussion_flag character(1)
  , discussion_item text
  , business_pass_status character(1)
  , business_pass_comment text
  , government_confirm_status character(1)
  , government_confirm_datetime timestamp
  , government_confirm_comment text
  , government_confirm_notified_flag character(1)
  , permission_judgement_result character(1)
  , answer_status character(1) not null
  , answer_data_type character(1) not null
  , register_status character(1)
  , delete_unnotified_flag character(1)
  , delete_flag character(1)
  , deadline_datetime timestamp
  , answer_permission_flag character(1)
  , government_confirm_permission_flag character(1)
  , version_information integer
  , permission_judgement_migration_flag char(1)
  , business_answer_datetime timestamp
  , constraint o_answer_PKC primary key (answer_id)
) ;

alter table o_answer add constraint idx_o_answer
  unique (application_id,application_step_id,judgement_id,department_answer_id,answer_id) ;

-- M_レイヤ
drop table if exists m_layer cascade;

create table m_layer (
  layer_id character varying(10) not null
  , layer_type character(1)
  , layer_name character varying(256)
  , table_name character varying(30)
  , layer_code character varying(256)
  , layer_query character varying(256) DEFAULT ''
  , query_require_flag character(1)
  , constraint m_layer_PKC primary key (layer_id)
) ;

create unique index idx_m_layer
  on m_layer(layer_id);

-- M_申請者情報項目
drop table if exists m_applicant_information_item cascade;

create table m_applicant_information_item (
  applicant_information_item_id character varying(10) not null
  , display_order integer
  , display_flag CHAR(1)
  , require_flag CHAR(1)
  , item_name character varying(256)
  , regex character varying(256)
  , mail_address CHAR(1)
  , search_condition_flag CHAR(1)
  , item_type CHAR(1) not null
  , application_step text
  , add_information_item_flag char(1)
  , contact_address_flag char(1)
  , constraint m_applicant_information_item_PKC primary key (applicant_information_item_id)
) ;

alter table m_applicant_information_item add constraint idx_m_applicant_information_item
  unique (applicant_information_item_id) ;

-- M_ラベル
drop table if exists m_label cascade;

create table m_label (
  view_code character varying(10) not null
  , label_id character varying(10) not null
  , label_key character varying(50)
  , label_type char(1)
  , label_text text
  , application_step text not null
  , constraint m_label_PKC primary key (view_code,label_id)
) ;

create unique index idx_m_label
  on m_label(view_code,label_id);

-- O_申請者情報
drop table if exists o_applicant_information cascade;

create table o_applicant_information (
  application_id integer not null
  , applicant_id integer not null
  , item_1 text
  , item_2 text
  , item_3 text
  , item_4 text
  , item_5 text
  , item_6 text
  , item_7 text
  , item_8 text
  , item_9 text
  , item_10 text
  , mail_address character varying(100)
  , collation_id character varying(20)
  , password character varying(1024)
  , contact_address_flag char(1)
  , constraint o_applicant_information_PKC primary key (applicant_id)
) ;

alter table o_applicant_information add constraint idx_o_applicant_information
  unique (applicant_id) ;

-- M_部署
drop table if exists m_department cascade;

create table m_department (
  department_id character varying(10) not null
  , department_name text
  , mail_address text
  , admin_mail_address text
  , answer_authority_flag char(1)
  , constraint m_department_PKC primary key (department_id)
) ;

create unique index idx_m_department
  on m_department(department_id);

-- O_申請
drop table if exists o_application cascade;

create table o_application (
  application_id integer not null
  , applicant_id integer
  , status character varying(3)
  , register_status character(1)
  , collation_text character varying(100)
  , register_datetime timestamp
  , update_datetime timestamp
  , application_type_id integer
  , constraint o_application_PKC primary key (application_id)
) ;

alter table o_application add constraint idx_o_application
  unique (application_id) ;

-- M_申請ファイル
drop table if exists m_application_file cascade;

create table m_application_file (
  application_file_id character varying(10) not null
  , judgement_item_id character varying(10) not null
  , require_flag char(1)
  , upload_file_name text
  , extension character varying(200)
  , application_file_type char(1)
  , constraint m_application_file_PKC primary key (application_file_id,judgement_item_id)
) ;

create unique index idx_m_application_file
  on m_application_file(judgement_item_id,application_file_id);

-- M_区分判定
drop table if exists m_category_judgement cascade;

create table m_category_judgement (
  judgement_item_id character varying(10) not null
  , gis_judgement char(1)
  , buffer double precision
  , display_attribute_flag char(1)
  , judgement_layer character varying(100)
  , table_name text
  , field_name text
  , non_applicable_layer_display_flag char(1)
  , simultaneous_display_layer text
  , simultaneous_display_layer_flag char(1)
  , disp_order double precision
  , constraint m_category_judgement_PKC primary key (judgement_item_id)
) ;

create unique index idx_m_category_judgement
  on m_category_judgement(judgement_item_id);

-- M_申請区分
drop table if exists m_application_category cascade;

create table m_application_category (
  category_id character varying(10) not null
  , view_id character varying(10) not null
  , "order" integer
  , label_name character varying(256)
  , constraint m_application_category_PKC primary key (category_id,view_id)
) ;

create unique index idx_m_application_category
  on m_application_category(view_id,category_id);

-- M_申請区分選択画面
drop table if exists m_application_category_selection_view cascade;

create table m_application_category_selection_view (
  view_id character varying(10) not null
  , view_flag character(1)
  , multiple_flag character(1)
  , require_flag character(1)
  , title text
  , description text
  , judgement_type text
  , constraint m_application_category_selection_view_PKC primary key (view_id)
) ;

create unique index idx_m_application_category_selection_view
  on m_application_category_selection_view(view_id);

-- M_カレンダ
drop table if exists m_calendar cascade;

create table m_calendar (
  cal_date date not null
  , week_day integer
  , biz_day_flag character(1)
  , comment character varying(25)
  , constraint m_calendar_PKC primary key (cal_date)
) ;

-- O_開発登録簿
drop table if exists o_development_document cascade;

create table o_development_document (
  file_id integer not null
  , application_id integer not null
  , development_document_id integer not null
  , file_path text
  , register_datetime timestamp
  , constraint o_development_document_PKC primary key (file_id)
) ;

create unique index idx_o_development_document
  on o_development_document(file_id);

-- M_開発登録簿
drop table if exists m_development_document cascade;

create table m_development_document (
  development_document_id integer not null
  , document_name text
  , document_type char(1)
  , constraint m_development_document_PKC primary key (development_document_id)
) ;

create unique index idx_m_development_document
  on m_development_document(development_document_id);

-- O_受付回答
drop table if exists o_accepting_answer cascade;

create table o_accepting_answer (
  accepting_answer_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , version_infomation integer not null
  , judgement_id character varying(10) not null
  , department_id character varying(10)
  , judgement_result text
  , judgement_result_index integer default 0
  , answer_content text
  , register_datetime timestamp
  , update_datetime timestamp
  , answer_data_type character(1) not null
  , register_status character(1)
  , deadline_datetime timestamp
  , answer_id integer
  , constraint o_accepting_answer_PKC primary key (accepting_answer_id)
) ;

alter table o_accepting_answer add constraint idx_o_accepting_answer
  unique (application_id,application_step_id,judgement_id,accepting_answer_id) ;

-- M_帳票ラベル
drop table if exists m_ledger_label cascade;

create table m_ledger_label (
  ledger_label_id character varying(10)
  , ledger_id character varying(10)
  , replace_identify text
  , table_name text
  , export_column_name text
  , filter_column_name text
  , filter_condition text
  , item_id_1 text
  , item_id_2 text
  , convert_order text
  , convert_format text
  , constraint m_ledger_label_PKC primary key (ledger_label_id)
) ;

-- O_部署回答
drop table if exists o_department_answer cascade;

create table o_department_answer (
  department_answer_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , department_id character varying(10) not null
  , government_confirm_status character(1)
  , government_confirm_datetime timestamp
  , government_confirm_comment text
  , notified_text text
  , complete_flag character(1)
  , notified_flag character(1)
  , register_datetime timestamp
  , update_datetime timestamp
  , register_status character(1)
  , delete_unnotified_flag character(1)
  , government_confirm_notified_flag character(1)
  , government_confirm_permission_flag character(1)
  , constraint o_department_answer_PKC primary key (department_answer_id)
) ;

alter table o_department_answer add constraint idx_o_department_answer
  unique (department_answer_id) ;

-- M_区分判定_権限
drop table if exists m_judgement_authority cascade;

create table m_judgement_authority (
  judgement_item_id character varying(10) not null
  , department_id character varying(10) not null
  , constraint m_judgement_authority_PKC primary key (judgement_item_id,department_id)
) ;

create unique index idx_m_judgement_authority
  on m_judgement_authority(judgement_item_id,department_id);

-- O_申請版情報
drop table if exists o_application_version_information cascade;

create table o_application_version_information (
  application_id integer not null
  , application_step_id integer not null
  , version_information integer not null
  , accepting_flag char(1)
  , accept_version_information integer
  , register_datetime timestamp
  , update_datetime timestamp
  , complete_datetime timestamp
  , register_status character(1)
  , constraint o_application_version_information_PKC primary key (application_id,application_step_id)
) ;

-- O_申請追加情報
drop table if exists o_applicant_information_add cascade;

create table o_applicant_information_add (
  applicant_id integer not null
  , application_id integer not null
  , application_step_id integer
  , applicant_information_item_id character varying(10)
  , item_value text
  , version_information integer
  , constraint o_applicant_information_add_PKC primary key (applicant_id)
) ;

alter table o_applicant_information_add add constraint idx_o_applicant_information_add
  unique (applicant_id) ;

-- M_申請情報項目選択肢
drop table if exists m_applicant_information_item_option cascade;

create table m_applicant_information_item_option (
  applicant_information_item_option_id character varying(10) not null
  , applicant_information_item_id character varying(10)
  , display_order integer
  , applicant_information_item_option_name text
  , constraint m_applicant_information_item_option_PKC primary key (applicant_information_item_option_id)
) ;

create unique index idx_m_applicant_information_item_option
  on m_applicant_information_item_option(applicant_information_item_option_id);

-- M_権限
drop table if exists m_authority cascade;

create table m_authority (
  department_id character varying(10) not null
  , application_step_id integer not null
  , answer_authority_flag character(1) default 0
  , notification_authority_flag character(1) default 0
  , constraint m_authority_PKC primary key (department_id,application_step_id)
) ;

create unique index idx_m_authority
  on m_authority(department_id,application_step_id);

-- O_帳票
drop table if exists o_ledger cascade;

create table o_ledger (
  file_id integer not null
  , application_id integer not null
  , application_step_id integer not null
  , ledger_id character varying(10) not null
  , file_name text
  , file_path text
  , notify_file_path text
  , register_datetime timestamp
  , receipt_datetime timestamp
  , notify_flag char(1)
  , constraint o_ledger_PKC primary key (file_id)
) ;

create unique index idx_o_ledger
  on o_ledger(file_id);

-- M_帳票
drop table if exists m_ledger cascade;

create table m_ledger (
  ledger_id character varying(10) not null
  , application_step_id integer
  , ledger_name text
  , display_name text
  , template_path text
  , output_type char(1)
  , notification_flag char(1)
  , ledger_type char(1)
  , update_flag char(1)
  , notify_flag char(1)
  , upload_extension text
  , information_text text
  , constraint m_ledger_PKC primary key (ledger_id)
) ;

create unique index idx_m_ledger
  on m_ledger(ledger_id);

-- M_申請段階
drop table if exists m_application_step cascade;

create table m_application_step (
  application_step_id integer not null
  , application_step_name text not null
  , constraint m_application_step_PKC primary key (application_step_id)
) ;

create unique index idx_m_application_step
  on m_application_step(application_step_id);

-- M_申請種類
drop table if exists m_application_type cascade;

create table m_application_type (
  application_type_id integer not null
  , application_type_name text not null
  , application_step text not null
  , constraint m_application_type_PKC primary key (application_type_id)
) ;

create unique index idx_m_application_type
  on m_application_type(application_type_id);

-- M_申請区分_区分判定
drop table if exists m_application_category_judgement cascade;

create table m_application_category_judgement (
  judgement_item_id character varying(10) not null
  , view_id character varying(10) not null
  , category_id character varying(10) not null
  , constraint m_application_category_judgement_PKC primary key (judgement_item_id,view_id,category_id)
) ;

-- M_判定結果
drop table if exists m_judgement_result cascade;

create table m_judgement_result (
  judgement_item_id character varying(10) not null
  , application_type_id integer not null
  , application_step_id integer not null
  , department_id character varying(10) not null
  , title text
  , applicable_summary text
  , applicable_description text DEFAULT ''
  , non_applicable_display_flag character(1)
  , non_applicable_summary text
  , non_applicable_description text
  , answer_require_flag character(1)
  , default_answer text
  , answer_editable_flag character(1)
  , answer_days integer
  , constraint m_judgement_result_PKC primary key (judgement_item_id,application_type_id,application_step_id,department_id)
) ;

create unique index idx_m_judgement_result
  on m_judgement_result(judgement_item_id,application_type_id,application_step_id,department_id);

-- F_申請地番
drop table if exists f_application_lot_number cascade;

create table f_application_lot_number (
  application_id integer not null
  , geom geometry
  , lot_numbers text
  , constraint f_application_lot_number_PKC primary key (application_id)
) ;

comment on table o_inquiry_address is 'O_問合せ宛先';
comment on column o_inquiry_address.inquiry_address_id is '問合せ宛先ID';
comment on column o_inquiry_address.message_id is 'メッセージID';
comment on column o_inquiry_address.department_id is '部署ID';
comment on column o_inquiry_address.read_flag is '既読フラグ:1:既読 0未読';
comment on column o_inquiry_address.answer_complete_flag is '回答済みフラグ:1: 回答済み 0: 未回答';

comment on table m_road_judge_label is 'M_道路判定ラベル';
comment on column m_road_judge_label.label_id is 'ラベルID';
comment on column m_road_judge_label.replace_identify is '置換識別子';
comment on column m_road_judge_label.index_value is 'インデックス値';
comment on column m_road_judge_label.index_text is 'インデックス文字列';
comment on column m_road_judge_label.min_value is '最小値';
comment on column m_road_judge_label.max_value is '最大値';
comment on column m_road_judge_label.replace_text is '置換テキスト';

comment on table o_inquiry_file is 'O_問合せファイル';
comment on column o_inquiry_file.inquiry_file_id is '問合せファイルID';
comment on column o_inquiry_file.message_id is 'メッセージID';
comment on column o_inquiry_file.file_name is 'ファイル名';
comment on column o_inquiry_file.file_path is 'ファイルパス';
comment on column o_inquiry_file.register_datetime is '登録日時';

comment on table m_answer_template is 'M_回答テンプレート';
comment on column m_answer_template.answer_template_id is '回答テンプレートID';
comment on column m_answer_template.disp_order is '表示順';
comment on column m_answer_template.answer_template_text is '回答テンプレートテキスト';
comment on column m_answer_template.judgement_item_id is '判定項目ID';

comment on table o_answer_file_history is 'O_回答ファイル更新履歴';
comment on column o_answer_file_history.answer_file_history_id is '回答ファイル履歴ID';
comment on column o_answer_file_history.answer_file_id is '回答ファイルID';
comment on column o_answer_file_history.answer_id is '回答ID';
comment on column o_answer_file_history.update_type is '更新タイプ:1:追加2:更新3:削除';
comment on column o_answer_file_history.update_user_id is '更新者ID';
comment on column o_answer_file_history.update_datetime is '更新日時';
comment on column o_answer_file_history.notify_flag is '通知フラグ:1:通知済み 0:未通知';

comment on table o_answer_history is 'O_回答履歴';
comment on column o_answer_history.answer_history_id is '回答履歴ID';
comment on column o_answer_history.answer_id is '回答ID';
comment on column o_answer_history.answer_user_id is '回答者ID';
comment on column o_answer_history.answer_datetime is '回答日時';
comment on column o_answer_history.answer_text is '回答文言';
comment on column o_answer_history.notify_flag is '通知フラグ:1:通知済み 0:未通知';
comment on column o_answer_history.discussion_item is '協議対象:選択され協議対象に対する帳票マスタIDはカンマ区切りで保持';
comment on column o_answer_history.business_pass_status is '事業者合否ステータス:0:否決、1:合意';
comment on column o_answer_history.business_pass_comment is '事業者合否コメント';
comment on column o_answer_history.government_confirm_status is '行政確定ステータス:0:合意、1:取下、2:却下';
comment on column o_answer_history.government_confirm_datetime is '行政確定日時';
comment on column o_answer_history.government_confirm_comment is '行政確定コメント';
comment on column o_answer_history.permission_judgement_result is '許可判定結果:0：問題なし、1:問題あり';
comment on column o_answer_history.re_application_flag is '再申請フラグ';
comment on column o_answer_history.discussion_flag is '事前協議フラグ';
comment on column o_answer_history.answer_status is 'ステータス';
comment on column o_answer_history.answer_data_type is 'データ種別';
comment on column o_answer_history.update_datetime is '更新日時';
comment on column o_answer_history.deadline_datetime is '回答期限日時';

comment on table o_message is 'O_メッセージ';
comment on column o_message.message_id is 'メッセージID';
comment on column o_message.chat_id is 'チャットID';
comment on column o_message.message_type is 'メッセージタイプ:1: 事業者→行政 2:行政→事業者 3:行政→行政';
comment on column o_message.sender_id is '送信者ID:送信者のユーザID.事業者の場合-1';
comment on column o_message.to_department_id is '宛先部署ID:宛先の部署ID.事業者の場合-1';
comment on column o_message.message_text is 'メッセージ本文';
comment on column o_message.send_datetime is '送信日時';
comment on column o_message.read_flag is '既読フラグ:1:既読 0未読';
comment on column o_message.answer_complete_flag is '回答済みフラグ:事業者→行政、行政→行政のメッセージタイプで参照。
1: 回答済み 0: 未回答';

comment on table o_chat is 'O_チャット';
comment on column o_chat.chat_id is 'チャットID';
comment on column o_chat.application_id is '申請ID';
comment on column o_chat.application_step_id is '申請段階ID';
comment on column o_chat.department_answer_id is '部署回答ID:事前協議以外設定不要';
comment on column o_chat.answer_id is '回答ID';
comment on column o_chat.government_answer_datetime is '行政回答日時';
comment on column o_chat.establishment_first_post_datetime is '事業者初回投稿日時';
comment on column o_chat.last_answerer_id is '最終回答者ID';
comment on column o_chat.establishment_post_datetime is '事業者投稿日時';

comment on table m_government_user is 'M_行政ユーザ';
comment on column m_government_user.user_id is 'ユーザID';
comment on column m_government_user.login_id is 'ログインID';
comment on column m_government_user.password is 'パスワード';
comment on column m_government_user.role_code is 'ロールコード:1: 事業者 2: 行政';
comment on column m_government_user.department_id is '部署ID';
comment on column m_government_user.user_name is '氏名';
comment on column m_government_user.admin_flag is '管理者フラグ:0:一般ユーザ、1:管理者';

comment on table m_application_search_result is 'M_申請情報検索結果';
comment on column m_application_search_result.application_search_result_id is '申請情報検索結果ID';
comment on column m_application_search_result.reference_type is '参照タイプ:0:申請区分 1:申請者情報 2:その他';
comment on column m_application_search_result.display_column_name is '表示カラム名:画面表示するカラム名';
comment on column m_application_search_result.display_order is '表示順';
comment on column m_application_search_result.table_name is 'テーブル名:参照テーブル名';
comment on column m_application_search_result.table_column_name is 'テーブルカラム名:参照カラム名';
comment on column m_application_search_result.response_key is 'レスポンスキー:レスポンスJSONのキー';
comment on column m_application_search_result.table_width is 'テーブル幅';

comment on table m_lot_number_search_result_definition is 'M_地番検索結果定義';
comment on column m_lot_number_search_result_definition.lot_number_search_definition_id is '地番検索結果定義ID';
comment on column m_lot_number_search_result_definition.display_order is '表示順';
comment on column m_lot_number_search_result_definition.table_type is 'テーブル種別:1:F_地番テーブル 0:F_大字テーブル';
comment on column m_lot_number_search_result_definition.display_column_name is '表示カラム名:画面表示するカラム名';
comment on column m_lot_number_search_result_definition.table_column_name is 'テーブルカラム名:地番検索結果表示カラム名を指定';
comment on column m_lot_number_search_result_definition.table_width is 'テーブル幅:%指定.データカラムの幅の合計で100%となるように設定.';
comment on column m_lot_number_search_result_definition.response_key is 'レスポンスキー:レスポンスJSONキー';

comment on table o_application_category is 'O_申請区分';
comment on column o_application_category.application_id is '申請ID';
comment on column o_application_category.view_id is '画面ID';
comment on column o_application_category.category_id is '申請区分ID';
comment on column o_application_category.application_step_id is '申請段階ID';
comment on column o_application_category.version_information is '版情報';

comment on table o_answer_file is 'O_回答ファイル';
comment on column o_answer_file.answer_file_id is '回答ファイルID:ファイルごとに付与される一意のID';
comment on column o_answer_file.answer_id is '回答ID:事前相談の場合設定';
comment on column o_answer_file.application_id is '申請ID';
comment on column o_answer_file.application_step_id is '申請段階ID';
comment on column o_answer_file.department_id is '部署ID:事前協議の場合設定';
comment on column o_answer_file.answer_file_name is '回答ファイル名';
comment on column o_answer_file.file_path is 'ファイルパス';
comment on column o_answer_file.notified_file_path is '通知済みファイルパス:通知済みの回答ファイルパス';
comment on column o_answer_file.delete_unnotified_flag is '削除未通知フラグ:1:削除済み・未通知';
comment on column o_answer_file.delete_flag is '削除フラグ:1:削除済み';

comment on table o_application_file is 'O_申請ファイル';
comment on column o_application_file.file_id is 'ファイルID:ファイルごとに付与される一意のID';
comment on column o_application_file.application_id is '申請ID';
comment on column o_application_file.application_step_id is '申請段階ID';
comment on column o_application_file.application_file_id is '申請ファイルID';
comment on column o_application_file.upload_file_name is 'アップロードファイル名';
comment on column o_application_file.file_path is 'ファイルパス';
comment on column o_application_file.extension is '拡張子:アップロードされたファイルの拡張子';
comment on column o_application_file.version_information is '版情報';
comment on column o_application_file.upload_datetime is 'アップロード日時';
comment on column o_application_file.direction_department is '指示元担当課';
comment on column o_application_file.revise_content is '修正内容';
comment on column o_application_file.delete_flag is '削除フラグ:1:削除済み';

comment on table o_answer is 'O_回答';
comment on column o_answer.answer_id is '回答ID';
comment on column o_answer.application_id is '申請ID';
comment on column o_answer.application_step_id is '申請段階ID';
comment on column o_answer.judgement_id is '判定項目ID';
comment on column o_answer.department_answer_id is '部署回答ID:事前協議以外はnull';
comment on column o_answer.department_id is '部署ID:事前協議の場合設定必須';
comment on column o_answer.judgement_result is '判定結果';
comment on column o_answer.judgement_result_index is '判定結果のインデックス:同一判定項目の複数行の判定結果のインデックス';
comment on column o_answer.answer_content is '回答内容:最新の回答内容';
comment on column o_answer.notified_text is '通知テキスト:通知済みの回答内容';
comment on column o_answer.register_datetime is '登録日時';
comment on column o_answer.update_datetime is '更新日時';
comment on column o_answer.complete_flag is '完了フラグ:1: 完了 0: 未完了';
comment on column o_answer.notified_flag is '通知フラグ:1:通知済み 0:未通知';
comment on column o_answer.answer_update_flag is '回答変更フラグ:1:あり、0:なし
※回答内容更新して、事業者へ未通知の状態で、変更ありとする';
comment on column o_answer.re_application_flag is '再申請フラグ:1:再申請 0:完了';
comment on column o_answer.business_reapplication_flag is '事業者再申請フラグ:事業者に通知済みの再申請フラグ 1:再申請 0:完了';
comment on column o_answer.discussion_flag is '事前協議フラグ:1:要事前協議、0：完了';
comment on column o_answer.discussion_item is '協議対象:選択され協議対象に対する帳票マスタIDはカンマ区切りで保持';
comment on column o_answer.business_pass_status is '事業者合否ステータス:0:否決、 1:合意';
comment on column o_answer.business_pass_comment is '事業者合否コメント';
comment on column o_answer.government_confirm_status is '行政確定ステータス:0:合意 1:取下 2:却下';
comment on column o_answer.government_confirm_datetime is '行政確定日時';
comment on column o_answer.government_confirm_comment is '行政確定コメント';
comment on column o_answer.government_confirm_notified_flag is '行政確定通知フラグ:1:通知済み 0:未通知';
comment on column o_answer.permission_judgement_result is '許可判定結果:0:問題なし、1:問題あり';
comment on column o_answer.answer_status is 'ステータス:0:未回答、1：回答済み、2：承認待ち、3：否認済み、4：承認済み、5：却下、6：同意済み';
comment on column o_answer.answer_data_type is 'データ種類:0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、
5:削除済み、6：引継、7:削除済み（行政）';
comment on column o_answer.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';
comment on column o_answer.delete_unnotified_flag is '削除未通知フラグ:1:削除済み・未通知';
comment on column o_answer.delete_flag is '削除フラグ:1:削除済み';
comment on column o_answer.deadline_datetime is '回答期限日時';
comment on column o_answer.answer_permission_flag is '回答通知許可フラグ:1:許可済み 0:未許可';
comment on column o_answer.government_confirm_permission_flag is '行政確定通知許可フラグ:1:許可済み 0:未許可';
comment on column o_answer.version_information is '版情報:ソートで使用';
comment on column o_answer.permission_judgement_migration_flag is '許可判定移行フラグ:1:許可判定移行時チェックしない';
comment on column o_answer.business_answer_datetime is '事業者回答登録日時';

comment on table m_layer is 'M_レイヤ';
comment on column m_layer.layer_id is 'レイヤID';
comment on column m_layer.layer_type is 'レイヤ種別:1: 判定対象レイヤ 0: 関連レイヤ';
comment on column m_layer.layer_name is 'レイヤ名:画面に表示するレイヤ名';
comment on column m_layer.table_name is 'テーブル名:フィーチャテーブル格納テーブル名';
comment on column m_layer.layer_code is 'レイヤコード:GeoServer上のレイヤID';
comment on column m_layer.layer_query is 'レイヤクエリ:GeoServerにリクエストを投げる際のクエリ文字列';
comment on column m_layer.query_require_flag is 'クエリ必須フラグ:1: 必須 0:不要';

comment on table m_label is 'M_ラベル';
comment on column m_label.view_code is '画面コード';
comment on column m_label.label_id is 'ラベルID';
comment on column m_label.label_key is 'ラベルキー';
comment on column m_label.label_type is '種別:0:事業者行政ともに使用 1:事業者のみ使用 2:行政のみ使用';
comment on column m_label.label_text is 'テキスト';
comment on column m_label.application_step is '申請段階:申請段階IDはカンマ区切りで保持
申請段階IDを問わず、常に表示する場合、「all」で書く';

comment on table o_applicant_information is 'O_申請者情報';
comment on column o_applicant_information.application_id is '申請ID';
comment on column o_applicant_information.applicant_id is '申請者情報ID';
comment on column o_applicant_information.item_1 is '項目1:申請者情報項目ID=1001の登録値を格納';
comment on column o_applicant_information.item_2 is '項目2:申請者情報項目ID=1002の登録値を格納';
comment on column o_applicant_information.item_3 is '項目3:申請者情報項目ID=1003の登録値を格納';
comment on column o_applicant_information.item_4 is '項目4:申請者情報項目ID=1004の登録値を格納';
comment on column o_applicant_information.item_5 is '項目5:申請者情報項目ID=1005の登録値を格納';
comment on column o_applicant_information.item_6 is '項目6:申請者情報項目ID=1006の登録値を格納';
comment on column o_applicant_information.item_7 is '項目7:申請者情報項目ID=1007の登録値を格納';
comment on column o_applicant_information.item_8 is '項目8:申請者情報項目ID=1008の登録値を格納';
comment on column o_applicant_information.item_9 is '項目9:申請者情報項目ID=1009の登録値を格納';
comment on column o_applicant_information.item_10 is '項目10:申請者情報項目ID=1010の登録値を格納';
comment on column o_applicant_information.mail_address is 'メールアドレス:通知に使用するメールアドレス';
comment on column o_applicant_information.collation_id is '照合ID';
comment on column o_applicant_information.password is 'パスワード:ハッシュ化して格納';
comment on column o_applicant_information.contact_address_flag is '連絡先フラグ:1:連絡先 0:申請者情報';

comment on table m_department is 'M_部署';
comment on column m_department.department_id is '部署ID';
comment on column m_department.department_name is '部署名';
comment on column m_department.mail_address is 'メールアドレス:通知先のメールアドレスをカンマ区切りで保持';
comment on column m_department.admin_mail_address is '管理者メールアドレス:該当部署の管理者の通知先のメールアドレスをカンマ区切りで保持';
comment on column m_department.answer_authority_flag is '回答権限フラグ:1:統括部署';

comment on table o_application is 'O_申請';
comment on column o_application.application_id is '申請ID';
comment on column o_application.applicant_id is '申請者情報ID';
comment on column o_application.status is 'ステータス';
comment on column o_application.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';
comment on column o_application.collation_text is '照合文字列:本登録時の照合文字列';
comment on column o_application.register_datetime is '登録日時';
comment on column o_application.update_datetime is '更新日時';
comment on column o_application.application_type_id is '申請種類ID';

comment on table m_application_file is 'M_申請ファイル';
comment on column m_application_file.application_file_id is '申請ファイルID';
comment on column m_application_file.judgement_item_id is '判定項目ID';
comment on column m_application_file.require_flag is '必須有無:1:必須 0:任意 2:任意(注意文言あり)';
comment on column m_application_file.upload_file_name is 'アップロードファイル名';
comment on column m_application_file.extension is '拡張子:利用可能な拡張子をカンマ区切り';
comment on column m_application_file.application_file_type is '申請ファイル種別:1:開発登録簿に含める';

comment on table m_category_judgement is 'M_区分判定';
comment on column m_category_judgement.judgement_item_id is '判定項目ID';
comment on column m_category_judgement.gis_judgement is 'GIS判定:0: GIS判定なし 1: 重なり判定 2: 重ならない判定 3: バッファ判定 4: バッファ重ならない判定 5:道路判定';
comment on column m_category_judgement.buffer is 'バッファ:バッファ判定時のバッファ半径 (m)';
comment on column m_category_judgement.display_attribute_flag is '重なり属性表示フラグ:0: 属性表示なし 1: 区切り文字で区切って表示 2:改行表示 3:概況診断結果一覧テーブルで行を分けて表示';
comment on column m_category_judgement.judgement_layer is '判定対象レイヤ:GIS判定で使用するレイヤのレイヤID（カンマ区切り）';
comment on column m_category_judgement.table_name is 'テーブル名:文言のラベルとして使用するテーブル名';
comment on column m_category_judgement.field_name is 'フィールド名:文言のラベルとして使用するフィールド名';
comment on column m_category_judgement.non_applicable_layer_display_flag is '判定レイヤ非該当時表示有無:1:表示 0:非表示';
comment on column m_category_judgement.simultaneous_display_layer is '同時表示レイヤ:該当診断結果画面で同時表示する関連レイヤのレイヤID（カンマ区切り）';
comment on column m_category_judgement.simultaneous_display_layer_flag is '同時表示レイヤ表示有無:1:表示 0: 非表示';
comment on column m_category_judgement.disp_order is '表示順';

comment on table m_application_category is 'M_申請区分';
comment on column m_application_category.category_id is '申請区分ID';
comment on column m_application_category.view_id is '画面ID';
comment on column m_application_category.order is '昇順';
comment on column m_application_category.label_name is '選択肢名';

comment on table m_application_category_selection_view is 'M_申請区分選択画面';
comment on column m_application_category_selection_view.view_id is '画面ID';
comment on column m_application_category_selection_view.view_flag is '表示有無:1=表示, 0=非表示';
comment on column m_application_category_selection_view.multiple_flag is '複数選択有無:1=複数選択なし 0=複数選択あり';
comment on column m_application_category_selection_view.require_flag is '必須有無:1=選択必須 0=選択任意';
comment on column m_application_category_selection_view.title is 'タイトル';
comment on column m_application_category_selection_view.description is '説明文';
comment on column m_application_category_selection_view.judgement_type is '申請種類:1=開発許可, 0=建築確認
カンマ区切りで格納';

comment on table m_calendar is 'M_カレンダ';
comment on column m_calendar.cal_date is 'カレンダー日付:日付';
comment on column m_calendar.week_day is '曜日:1:日曜日、2:月曜日、3:火曜日、4:水曜日、5:木曜日、6;金曜日、7:土曜日';
comment on column m_calendar.biz_day_flag is '営業日フラグ:1:営業日、0:非営業日';
comment on column m_calendar.comment is '備考:備考';

comment on table o_development_document is 'O_開発登録簿';
comment on column o_development_document.file_id is 'ファイルID';
comment on column o_development_document.application_id is '申請ID';
comment on column o_development_document.development_document_id is '開発登録簿マスタID';
comment on column o_development_document.file_path is 'ファイルパス';
comment on column o_development_document.register_datetime is '作成日時';

comment on table m_development_document is 'M_開発登録簿';
comment on column m_development_document.development_document_id is '開発登録簿マスタID:1:最終提出書類 2:全提出書類 3:開発登録簿 固定';
comment on column m_development_document.document_name is '書類名';
comment on column m_development_document.document_type is '書類種類:1:開発登録簿（帳票アップロード時に業務データ生成）';

comment on table o_accepting_answer is 'O_受付回答:事前協議受付回答';
comment on column o_accepting_answer.accepting_answer_id is '受付回答ID';
comment on column o_accepting_answer.application_id is '申請ID';
comment on column o_accepting_answer.application_step_id is '申請段階ID';
comment on column o_accepting_answer.version_infomation is '版情報';
comment on column o_accepting_answer.judgement_id is '判定項目ID';
comment on column o_accepting_answer.department_id is '部署ID:事前協議の場合設定必須';
comment on column o_accepting_answer.judgement_result is '判定結果';
comment on column o_accepting_answer.judgement_result_index is '判定結果のインデックス:同一判定項目の複数行の判定結果のインデックス';
comment on column o_accepting_answer.answer_content is '回答内容:最新の回答内容';
comment on column o_accepting_answer.register_datetime is '登録日時';
comment on column o_accepting_answer.update_datetime is '更新日時';
comment on column o_accepting_answer.answer_data_type is 'データ種類:0:登録、1:更新、2：追加、3:行政で追加、4:一律追加、
5:削除済み、6：引継、7:削除済み（行政）';
comment on column o_accepting_answer.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';
comment on column o_accepting_answer.deadline_datetime is '回答期限日時';
comment on column o_accepting_answer.answer_id is '回答ID:事前協議受付時にO_回答に該当レコードが存在しない場合Insert存在する場合Update';

comment on table m_applicant_information_item is 'M_申請者情報項目';
comment on column m_applicant_information_item.applicant_information_item_id is '申請者情報項目ID';
comment on column m_applicant_information_item.display_order is '昇順';
comment on column m_applicant_information_item.display_flag is '表示有無';
comment on column m_applicant_information_item.require_flag is '必須有無';
comment on column m_applicant_information_item.item_name is '項目名';
comment on column m_applicant_information_item.regex is '正規表現:空の場合正規表現チェックはしない';
comment on column m_applicant_information_item.mail_address is 'メールアドレス:1=メールアドレス';
comment on column m_applicant_information_item.search_condition_flag is '検索条件表示有無';
comment on column m_applicant_information_item.item_type is '項目型:0:1行のみの入力欄で表示、
1:複数行の入力欄で表示、
2:日付（カレンダー）、
3:数値、
4:ドロップダウン単一選択
5:ドロップダウン複数選択';
comment on column m_applicant_information_item.application_step is '申請段階:申請段階IDはカンマ区切りで保持';
comment on column m_applicant_information_item.add_information_item_flag is '追加情報フラグ:0:申請者情報項目、1:申請追加情報項目';
comment on column m_applicant_information_item.contact_address_flag is '連絡先フラグ:1:連絡先として表示 0:連絡先として表示しない';

comment on table m_ledger_label is 'M_帳票ラベル';
comment on column m_ledger_label.ledger_label_id is '帳票ラベルID';
comment on column m_ledger_label.ledger_id is '帳票マスタID';
comment on column m_ledger_label.replace_identify is '置換識別子';
comment on column m_ledger_label.table_name is 'テーブル名:出力に使用するテーブル名';
comment on column m_ledger_label.export_column_name is '出力カラム名:出力に使用するカラム名';
comment on column m_ledger_label.filter_column_name is 'フィルタカラム名';
comment on column m_ledger_label.filter_condition is 'フィルタ条件';
comment on column m_ledger_label.item_id_1 is '項目ID1';
comment on column m_ledger_label.item_id_2 is '項目ID2';
comment on column m_ledger_label.convert_order is '変換オーダ:変換対象1=変換値,変換対象2=変換値,…のフォーマットで指定 
round=x 丸める桁数
dateformat=yyyy年mm月dd日 日付の出力フォーマット
day=x 加減算する日数
separate=comma 区切り文字（カンマの場合comma）
japanese=true 和暦表示するか否か';
comment on column m_ledger_label.convert_format is '変換フォーマット:変換後の文字列を埋め込むフォーマット %sで埋込文字列を指定';

comment on table o_department_answer is 'O_部署回答';
comment on column o_department_answer.department_answer_id is '部署回答ID';
comment on column o_department_answer.application_id is '申請ID';
comment on column o_department_answer.application_step_id is '申請段階ID';
comment on column o_department_answer.department_id is '部署ID';
comment on column o_department_answer.government_confirm_status is '行政確定ステータス:0:合意 1:取下 2:却下';
comment on column o_department_answer.government_confirm_datetime is '行政確定日時';
comment on column o_department_answer.government_confirm_comment is '行政確定コメント';
comment on column o_department_answer.notified_text is '通知テキスト:通知済みの行政確定コメント';
comment on column o_department_answer.complete_flag is '完了フラグ:1: 完了 0: 未完了';
comment on column o_department_answer.notified_flag is '通知フラグ:1:通知済み 0:未通知';
comment on column o_department_answer.register_datetime is '登録日時';
comment on column o_department_answer.update_datetime is '更新日時';
comment on column o_department_answer.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';
comment on column o_department_answer.delete_unnotified_flag is '削除未通知フラグ:1:削除済み・未通知';
comment on column o_department_answer.government_confirm_notified_flag is '行政確定通知フラグ:1:通知済み 0:未通知';
comment on column o_department_answer.government_confirm_permission_flag is '行政確定通知許可フラグ:1:許可済み 0:未許可';

comment on table m_judgement_authority is 'M_区分判定_権限';
comment on column m_judgement_authority.judgement_item_id is '判定項目ID';
comment on column m_judgement_authority.department_id is '部署ID';

comment on table o_application_version_information is 'O_申請版情報';
comment on column o_application_version_information.application_id is '申請ID';
comment on column o_application_version_information.application_step_id is '申請段階ID';
comment on column o_application_version_information.version_information is '版情報';
comment on column o_application_version_information.accepting_flag is '受付フラグ:事前協議受付状態を管理.';
comment on column o_application_version_information.accept_version_information is '受付版情報:受付された最終の版情報';
comment on column o_application_version_information.register_datetime is '登録日時';
comment on column o_application_version_information.update_datetime is '更新日時';
comment on column o_application_version_information.complete_datetime is '完了日時';
comment on column o_application_version_information.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';

comment on table o_applicant_information_add is 'O_申請追加情報';
comment on column o_applicant_information_add.applicant_id is '申請追加情報ID';
comment on column o_applicant_information_add.application_id is '申請ID';
comment on column o_applicant_information_add.application_step_id is '申請段階ID';
comment on column o_applicant_information_add.applicant_information_item_id is '申請者情報項目ID';
comment on column o_applicant_information_add.item_value is '項目値:ドロップダウン複数選択の値はカンマ区切りで保持';
comment on column o_applicant_information_add.version_information is '版情報:0: 仮申請中 1: 申請済み';

comment on table m_applicant_information_item_option is 'M_申請情報項目選択肢';
comment on column m_applicant_information_item_option.applicant_information_item_option_id is '申請情報項目選択肢ID';
comment on column m_applicant_information_item_option.applicant_information_item_id is '申請者情報項目ID';
comment on column m_applicant_information_item_option.display_order is '昇順';
comment on column m_applicant_information_item_option.applicant_information_item_option_name is '選択肢名';

comment on table m_authority is 'M_権限';
comment on column m_authority.department_id is '部署ID';
comment on column m_authority.application_step_id is '申請段階ID';
comment on column m_authority.answer_authority_flag is '回答権限フラグ:0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）';
comment on column m_authority.notification_authority_flag is '通知権限フラグ:0: 権限なし 1: 権限あり（所属部署のみ操作可）、2：権限あり（他部署も操作可）';

comment on table o_ledger is 'O_帳票';
comment on column o_ledger.file_id is 'ファイルID';
comment on column o_ledger.application_id is '申請ID';
comment on column o_ledger.application_step_id is '申請段階ID';
comment on column o_ledger.ledger_id is '帳票マスタID';
comment on column o_ledger.file_name is 'ファイル名';
comment on column o_ledger.file_path is 'ファイルパス';
comment on column o_ledger.notify_file_path is '通知ファイルパス';
comment on column o_ledger.register_datetime is '作成日時';
comment on column o_ledger.receipt_datetime is '受領日時';
comment on column o_ledger.notify_flag is '通知フラグ:1:事業者に通知済 0:事業者に未通知';

comment on table m_ledger is 'M_帳票';
comment on column m_ledger.ledger_id is '帳票マスタID';
comment on column m_ledger.application_step_id is '申請段階ID';
comment on column m_ledger.ledger_name is '帳票名';
comment on column m_ledger.display_name is '画面表示名:出力種類が１の場合、表示名が設定必要';
comment on column m_ledger.template_path is 'テンプレートパス';
comment on column m_ledger.output_type is '出力種類:0:常に出力、1：画面に選択されたレコードがあれば出力';
comment on column m_ledger.notification_flag is '受領時通知要否:事業者側で帳票をダウンロードする時、行政へ通知要否を保持
0:通知不要、1:通知必要';
comment on column m_ledger.ledger_type is '帳票種類:1:開発登録簿に含める帳票';
comment on column m_ledger.update_flag is '更新フラグ:1:更新可能 0:更新不可';
comment on column m_ledger.notify_flag is '通知フラグ:1:通知必要 0:通知不要';
comment on column m_ledger.upload_extension is 'アップロード時拡張子';
comment on column m_ledger.information_text is '案内テキスト';

comment on table m_application_step is 'M_申請段階';
comment on column m_application_step.application_step_id is '申請段階ID';
comment on column m_application_step.application_step_name is '申請段階名';

comment on table m_application_type is 'M_申請種類';
comment on column m_application_type.application_type_id is '申請種類ID';
comment on column m_application_type.application_type_name is '申請種類名';
comment on column m_application_type.application_step is '申請段階:申請段階IDは実施順でカンマ区切りで保持
例）開発許可：「1：事前相談」　⇒　「2：事前協議」　⇒　「3：許可判定」
　　「1,2,3」で格納';

comment on table m_application_category_judgement is 'M_申請区分_区分判定';
comment on column m_application_category_judgement.judgement_item_id is '判定項目ID';
comment on column m_application_category_judgement.view_id is '画面ID';
comment on column m_application_category_judgement.category_id is '申請区分ID';

comment on table m_judgement_result is 'M_判定結果';
comment on column m_judgement_result.judgement_item_id is '判定項目ID';
comment on column m_judgement_result.application_type_id is '申請種類ID';
comment on column m_judgement_result.application_step_id is '申請段階ID';
comment on column m_judgement_result.department_id is '部署ID:事前協議以外では[-1]';
comment on column m_judgement_result.title is 'タイトル';
comment on column m_judgement_result.answer_days is '回答日数';
comment on column m_judgement_result.default_answer is 'デフォルト回答:回答任意の項目に登録する初期回答文言';
comment on column m_judgement_result.applicable_summary is '該当表示概要';
comment on column m_judgement_result.applicable_description is '該当表示文言';
comment on column m_judgement_result.non_applicable_display_flag is '非該当表示有無:1:表示 0:非表示';
comment on column m_judgement_result.non_applicable_summary is '非該当表示概要';
comment on column m_judgement_result.non_applicable_description is '非該当表示文言';
comment on column m_judgement_result.answer_require_flag is '回答必須フラグ:1:回答必須 0:回答任意';
comment on column m_judgement_result.answer_editable_flag is '編集可能フラグ :1:編集可能 0:編集不可';

comment on table f_application_lot_number is 'F_申請地番';
comment on column f_application_lot_number.application_id is '申請ID';
comment on column f_application_lot_number.geom is 'ジオメトリ:マルチポリゴン.座標系はF_地番と共通';
comment on column f_application_lot_number.lot_numbers is '地番一覧';