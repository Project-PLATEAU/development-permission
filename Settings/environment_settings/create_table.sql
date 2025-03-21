-- �J�����\���Ǘ��V�X�e���e�[�u���쐬DDL
-- �������s���e�[�u�����쐬���Ă��������B

-- O_�⍇������
drop table if exists o_inquiry_address cascade;

create table o_inquiry_address (
  inquiry_address_id integer not null
  , message_id integer
  , department_id character varying(10)
  , read_flag character(1)
  , answer_complete_flag character(1)
  , constraint o_inquiry_address_PKC primary key (inquiry_address_id)
) ;

-- M_���H���胉�x��
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

-- O_�⍇���t�@�C��
drop table if exists o_inquiry_file cascade;

create table o_inquiry_file (
  inquiry_file_id integer not null
  , message_id integer
  , file_name varchar(255)
  , file_path text
  , register_datetime timestamp
  , constraint o_inquiry_file_PKC primary key (inquiry_file_id)
) ;

-- M_�񓚃e���v���[�g
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

-- O_�񓚃t�@�C���X�V����
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

-- O_�񓚗���
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

-- O_���b�Z�[�W
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

-- O_�`���b�g
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

-- M_�s�����[�U
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

-- M_�\����񌟍�����
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

-- M_�n�Ԍ������ʒ�`
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

-- O_�\���敪
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

-- O_�񓚃t�@�C��
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

-- O_�\���t�@�C��
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

-- O_��
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

-- M_���C��
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

-- M_�\���ҏ�񍀖�
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

-- M_���x��
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

-- O_�\���ҏ��
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

-- M_����
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

-- O_�\��
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

-- M_�\���t�@�C��
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

-- M_�敪����
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

-- M_�\���敪
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

-- M_�\���敪�I�����
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

-- M_�J�����_
drop table if exists m_calendar cascade;

create table m_calendar (
  cal_date date not null
  , week_day integer
  , biz_day_flag character(1)
  , comment character varying(25)
  , constraint m_calendar_PKC primary key (cal_date)
) ;

-- O_�J���o�^��
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

-- M_�J���o�^��
drop table if exists m_development_document cascade;

create table m_development_document (
  development_document_id integer not null
  , document_name text
  , document_type char(1)
  , constraint m_development_document_PKC primary key (development_document_id)
) ;

create unique index idx_m_development_document
  on m_development_document(development_document_id);

-- O_��t��
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

-- M_���[���x��
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

-- O_������
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

-- M_�敪����_����
drop table if exists m_judgement_authority cascade;

create table m_judgement_authority (
  judgement_item_id character varying(10) not null
  , department_id character varying(10) not null
  , constraint m_judgement_authority_PKC primary key (judgement_item_id,department_id)
) ;

create unique index idx_m_judgement_authority
  on m_judgement_authority(judgement_item_id,department_id);

-- O_�\���ŏ��
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

-- O_�\���ǉ����
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

-- M_�\����񍀖ڑI����
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

-- M_����
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

-- O_���[
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

-- M_���[
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

-- M_�\���i�K
drop table if exists m_application_step cascade;

create table m_application_step (
  application_step_id integer not null
  , application_step_name text not null
  , constraint m_application_step_PKC primary key (application_step_id)
) ;

create unique index idx_m_application_step
  on m_application_step(application_step_id);

-- M_�\�����
drop table if exists m_application_type cascade;

create table m_application_type (
  application_type_id integer not null
  , application_type_name text not null
  , application_step text not null
  , constraint m_application_type_PKC primary key (application_type_id)
) ;

create unique index idx_m_application_type
  on m_application_type(application_type_id);

-- M_�\���敪_�敪����
drop table if exists m_application_category_judgement cascade;

create table m_application_category_judgement (
  judgement_item_id character varying(10) not null
  , view_id character varying(10) not null
  , category_id character varying(10) not null
  , constraint m_application_category_judgement_PKC primary key (judgement_item_id,view_id,category_id)
) ;

-- M_���茋��
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

-- F_�\���n��
drop table if exists f_application_lot_number cascade;

create table f_application_lot_number (
  application_id integer not null
  , geom geometry
  , lot_numbers text
  , constraint f_application_lot_number_PKC primary key (application_id)
) ;

comment on table o_inquiry_address is 'O_�⍇������';
comment on column o_inquiry_address.inquiry_address_id is '�⍇������ID';
comment on column o_inquiry_address.message_id is '���b�Z�[�WID';
comment on column o_inquiry_address.department_id is '����ID';
comment on column o_inquiry_address.read_flag is '���ǃt���O:1:���� 0����';
comment on column o_inquiry_address.answer_complete_flag is '�񓚍ς݃t���O:1: �񓚍ς� 0: ����';

comment on table m_road_judge_label is 'M_���H���胉�x��';
comment on column m_road_judge_label.label_id is '���x��ID';
comment on column m_road_judge_label.replace_identify is '�u�����ʎq';
comment on column m_road_judge_label.index_value is '�C���f�b�N�X�l';
comment on column m_road_judge_label.index_text is '�C���f�b�N�X������';
comment on column m_road_judge_label.min_value is '�ŏ��l';
comment on column m_road_judge_label.max_value is '�ő�l';
comment on column m_road_judge_label.replace_text is '�u���e�L�X�g';

comment on table o_inquiry_file is 'O_�⍇���t�@�C��';
comment on column o_inquiry_file.inquiry_file_id is '�⍇���t�@�C��ID';
comment on column o_inquiry_file.message_id is '���b�Z�[�WID';
comment on column o_inquiry_file.file_name is '�t�@�C����';
comment on column o_inquiry_file.file_path is '�t�@�C���p�X';
comment on column o_inquiry_file.register_datetime is '�o�^����';

comment on table m_answer_template is 'M_�񓚃e���v���[�g';
comment on column m_answer_template.answer_template_id is '�񓚃e���v���[�gID';
comment on column m_answer_template.disp_order is '�\����';
comment on column m_answer_template.answer_template_text is '�񓚃e���v���[�g�e�L�X�g';
comment on column m_answer_template.judgement_item_id is '���荀��ID';

comment on table o_answer_file_history is 'O_�񓚃t�@�C���X�V����';
comment on column o_answer_file_history.answer_file_history_id is '�񓚃t�@�C������ID';
comment on column o_answer_file_history.answer_file_id is '�񓚃t�@�C��ID';
comment on column o_answer_file_history.answer_id is '��ID';
comment on column o_answer_file_history.update_type is '�X�V�^�C�v:1:�ǉ�2:�X�V3:�폜';
comment on column o_answer_file_history.update_user_id is '�X�V��ID';
comment on column o_answer_file_history.update_datetime is '�X�V����';
comment on column o_answer_file_history.notify_flag is '�ʒm�t���O:1:�ʒm�ς� 0:���ʒm';

comment on table o_answer_history is 'O_�񓚗���';
comment on column o_answer_history.answer_history_id is '�񓚗���ID';
comment on column o_answer_history.answer_id is '��ID';
comment on column o_answer_history.answer_user_id is '�񓚎�ID';
comment on column o_answer_history.answer_datetime is '�񓚓���';
comment on column o_answer_history.answer_text is '�񓚕���';
comment on column o_answer_history.notify_flag is '�ʒm�t���O:1:�ʒm�ς� 0:���ʒm';
comment on column o_answer_history.discussion_item is '���c�Ώ�:�I�����ꋦ�c�Ώۂɑ΂��钠�[�}�X�^ID�̓J���}��؂�ŕێ�';
comment on column o_answer_history.business_pass_status is '���Ǝҍ��ۃX�e�[�^�X:0:�ی��A1:����';
comment on column o_answer_history.business_pass_comment is '���Ǝҍ��ۃR�����g';
comment on column o_answer_history.government_confirm_status is '�s���m��X�e�[�^�X:0:���ӁA1:�扺�A2:�p��';
comment on column o_answer_history.government_confirm_datetime is '�s���m�����';
comment on column o_answer_history.government_confirm_comment is '�s���m��R�����g';
comment on column o_answer_history.permission_judgement_result is '�����茋��:0�F���Ȃ��A1:��肠��';
comment on column o_answer_history.re_application_flag is '�Đ\���t���O';
comment on column o_answer_history.discussion_flag is '���O���c�t���O';
comment on column o_answer_history.answer_status is '�X�e�[�^�X';
comment on column o_answer_history.answer_data_type is '�f�[�^���';
comment on column o_answer_history.update_datetime is '�X�V����';
comment on column o_answer_history.deadline_datetime is '�񓚊�������';

comment on table o_message is 'O_���b�Z�[�W';
comment on column o_message.message_id is '���b�Z�[�WID';
comment on column o_message.chat_id is '�`���b�gID';
comment on column o_message.message_type is '���b�Z�[�W�^�C�v:1: ���Ǝҁ��s�� 2:�s�������Ǝ� 3:�s�����s��';
comment on column o_message.sender_id is '���M��ID:���M�҂̃��[�UID.���Ǝ҂̏ꍇ-1';
comment on column o_message.to_department_id is '���敔��ID:����̕���ID.���Ǝ҂̏ꍇ-1';
comment on column o_message.message_text is '���b�Z�[�W�{��';
comment on column o_message.send_datetime is '���M����';
comment on column o_message.read_flag is '���ǃt���O:1:���� 0����';
comment on column o_message.answer_complete_flag is '�񓚍ς݃t���O:���Ǝҁ��s���A�s�����s���̃��b�Z�[�W�^�C�v�ŎQ�ƁB
1: �񓚍ς� 0: ����';

comment on table o_chat is 'O_�`���b�g';
comment on column o_chat.chat_id is '�`���b�gID';
comment on column o_chat.application_id is '�\��ID';
comment on column o_chat.application_step_id is '�\���i�KID';
comment on column o_chat.department_answer_id is '������ID:���O���c�ȊO�ݒ�s�v';
comment on column o_chat.answer_id is '��ID';
comment on column o_chat.government_answer_datetime is '�s���񓚓���';
comment on column o_chat.establishment_first_post_datetime is '���Ǝҏ��񓊍e����';
comment on column o_chat.last_answerer_id is '�ŏI�񓚎�ID';
comment on column o_chat.establishment_post_datetime is '���Ǝғ��e����';

comment on table m_government_user is 'M_�s�����[�U';
comment on column m_government_user.user_id is '���[�UID';
comment on column m_government_user.login_id is '���O�C��ID';
comment on column m_government_user.password is '�p�X���[�h';
comment on column m_government_user.role_code is '���[���R�[�h:1: ���Ǝ� 2: �s��';
comment on column m_government_user.department_id is '����ID';
comment on column m_government_user.user_name is '����';
comment on column m_government_user.admin_flag is '�Ǘ��҃t���O:0:��ʃ��[�U�A1:�Ǘ���';

comment on table m_application_search_result is 'M_�\����񌟍�����';
comment on column m_application_search_result.application_search_result_id is '�\����񌟍�����ID';
comment on column m_application_search_result.reference_type is '�Q�ƃ^�C�v:0:�\���敪 1:�\���ҏ�� 2:���̑�';
comment on column m_application_search_result.display_column_name is '�\���J������:��ʕ\������J������';
comment on column m_application_search_result.display_order is '�\����';
comment on column m_application_search_result.table_name is '�e�[�u����:�Q�ƃe�[�u����';
comment on column m_application_search_result.table_column_name is '�e�[�u���J������:�Q�ƃJ������';
comment on column m_application_search_result.response_key is '���X�|���X�L�[:���X�|���XJSON�̃L�[';
comment on column m_application_search_result.table_width is '�e�[�u����';

comment on table m_lot_number_search_result_definition is 'M_�n�Ԍ������ʒ�`';
comment on column m_lot_number_search_result_definition.lot_number_search_definition_id is '�n�Ԍ������ʒ�`ID';
comment on column m_lot_number_search_result_definition.display_order is '�\����';
comment on column m_lot_number_search_result_definition.table_type is '�e�[�u�����:1:F_�n�ԃe�[�u�� 0:F_�厚�e�[�u��';
comment on column m_lot_number_search_result_definition.display_column_name is '�\���J������:��ʕ\������J������';
comment on column m_lot_number_search_result_definition.table_column_name is '�e�[�u���J������:�n�Ԍ������ʕ\���J���������w��';
comment on column m_lot_number_search_result_definition.table_width is '�e�[�u����:%�w��.�f�[�^�J�����̕��̍��v��100%�ƂȂ�悤�ɐݒ�.';
comment on column m_lot_number_search_result_definition.response_key is '���X�|���X�L�[:���X�|���XJSON�L�[';

comment on table o_application_category is 'O_�\���敪';
comment on column o_application_category.application_id is '�\��ID';
comment on column o_application_category.view_id is '���ID';
comment on column o_application_category.category_id is '�\���敪ID';
comment on column o_application_category.application_step_id is '�\���i�KID';
comment on column o_application_category.version_information is '�ŏ��';

comment on table o_answer_file is 'O_�񓚃t�@�C��';
comment on column o_answer_file.answer_file_id is '�񓚃t�@�C��ID:�t�@�C�����Ƃɕt�^������ӂ�ID';
comment on column o_answer_file.answer_id is '��ID:���O���k�̏ꍇ�ݒ�';
comment on column o_answer_file.application_id is '�\��ID';
comment on column o_answer_file.application_step_id is '�\���i�KID';
comment on column o_answer_file.department_id is '����ID:���O���c�̏ꍇ�ݒ�';
comment on column o_answer_file.answer_file_name is '�񓚃t�@�C����';
comment on column o_answer_file.file_path is '�t�@�C���p�X';
comment on column o_answer_file.notified_file_path is '�ʒm�ς݃t�@�C���p�X:�ʒm�ς݂̉񓚃t�@�C���p�X';
comment on column o_answer_file.delete_unnotified_flag is '�폜���ʒm�t���O:1:�폜�ς݁E���ʒm';
comment on column o_answer_file.delete_flag is '�폜�t���O:1:�폜�ς�';

comment on table o_application_file is 'O_�\���t�@�C��';
comment on column o_application_file.file_id is '�t�@�C��ID:�t�@�C�����Ƃɕt�^������ӂ�ID';
comment on column o_application_file.application_id is '�\��ID';
comment on column o_application_file.application_step_id is '�\���i�KID';
comment on column o_application_file.application_file_id is '�\���t�@�C��ID';
comment on column o_application_file.upload_file_name is '�A�b�v���[�h�t�@�C����';
comment on column o_application_file.file_path is '�t�@�C���p�X';
comment on column o_application_file.extension is '�g���q:�A�b�v���[�h���ꂽ�t�@�C���̊g���q';
comment on column o_application_file.version_information is '�ŏ��';
comment on column o_application_file.upload_datetime is '�A�b�v���[�h����';
comment on column o_application_file.direction_department is '�w�����S����';
comment on column o_application_file.revise_content is '�C�����e';
comment on column o_application_file.delete_flag is '�폜�t���O:1:�폜�ς�';

comment on table o_answer is 'O_��';
comment on column o_answer.answer_id is '��ID';
comment on column o_answer.application_id is '�\��ID';
comment on column o_answer.application_step_id is '�\���i�KID';
comment on column o_answer.judgement_id is '���荀��ID';
comment on column o_answer.department_answer_id is '������ID:���O���c�ȊO��null';
comment on column o_answer.department_id is '����ID:���O���c�̏ꍇ�ݒ�K�{';
comment on column o_answer.judgement_result is '���茋��';
comment on column o_answer.judgement_result_index is '���茋�ʂ̃C���f�b�N�X:���ꔻ�荀�ڂ̕����s�̔��茋�ʂ̃C���f�b�N�X';
comment on column o_answer.answer_content is '�񓚓��e:�ŐV�̉񓚓��e';
comment on column o_answer.notified_text is '�ʒm�e�L�X�g:�ʒm�ς݂̉񓚓��e';
comment on column o_answer.register_datetime is '�o�^����';
comment on column o_answer.update_datetime is '�X�V����';
comment on column o_answer.complete_flag is '�����t���O:1: ���� 0: ������';
comment on column o_answer.notified_flag is '�ʒm�t���O:1:�ʒm�ς� 0:���ʒm';
comment on column o_answer.answer_update_flag is '�񓚕ύX�t���O:1:����A0:�Ȃ�
���񓚓��e�X�V���āA���Ǝ҂֖��ʒm�̏�ԂŁA�ύX����Ƃ���';
comment on column o_answer.re_application_flag is '�Đ\���t���O:1:�Đ\�� 0:����';
comment on column o_answer.business_reapplication_flag is '���ƎҍĐ\���t���O:���Ǝ҂ɒʒm�ς݂̍Đ\���t���O 1:�Đ\�� 0:����';
comment on column o_answer.discussion_flag is '���O���c�t���O:1:�v���O���c�A0�F����';
comment on column o_answer.discussion_item is '���c�Ώ�:�I�����ꋦ�c�Ώۂɑ΂��钠�[�}�X�^ID�̓J���}��؂�ŕێ�';
comment on column o_answer.business_pass_status is '���Ǝҍ��ۃX�e�[�^�X:0:�ی��A 1:����';
comment on column o_answer.business_pass_comment is '���Ǝҍ��ۃR�����g';
comment on column o_answer.government_confirm_status is '�s���m��X�e�[�^�X:0:���� 1:�扺 2:�p��';
comment on column o_answer.government_confirm_datetime is '�s���m�����';
comment on column o_answer.government_confirm_comment is '�s���m��R�����g';
comment on column o_answer.government_confirm_notified_flag is '�s���m��ʒm�t���O:1:�ʒm�ς� 0:���ʒm';
comment on column o_answer.permission_judgement_result is '�����茋��:0:���Ȃ��A1:��肠��';
comment on column o_answer.answer_status is '�X�e�[�^�X:0:���񓚁A1�F�񓚍ς݁A2�F���F�҂��A3�F�۔F�ς݁A4�F���F�ς݁A5�F�p���A6�F���Ӎς�';
comment on column o_answer.answer_data_type is '�f�[�^���:0:�o�^�A1:�X�V�A2�F�ǉ��A3:�s���Œǉ��A4:�ꗥ�ǉ��A
5:�폜�ς݁A6�F���p�A7:�폜�ς݁i�s���j';
comment on column o_answer.register_status is '�o�^�X�e�[�^�X:0: ���\���� 1: �\���ς�';
comment on column o_answer.delete_unnotified_flag is '�폜���ʒm�t���O:1:�폜�ς݁E���ʒm';
comment on column o_answer.delete_flag is '�폜�t���O:1:�폜�ς�';
comment on column o_answer.deadline_datetime is '�񓚊�������';
comment on column o_answer.answer_permission_flag is '�񓚒ʒm���t���O:1:���ς� 0:������';
comment on column o_answer.government_confirm_permission_flag is '�s���m��ʒm���t���O:1:���ς� 0:������';
comment on column o_answer.version_information is '�ŏ��:�\�[�g�Ŏg�p';
comment on column o_answer.permission_judgement_migration_flag is '������ڍs�t���O:1:������ڍs���`�F�b�N���Ȃ�';
comment on column o_answer.business_answer_datetime is '���Ǝ҉񓚓o�^����';

comment on table m_layer is 'M_���C��';
comment on column m_layer.layer_id is '���C��ID';
comment on column m_layer.layer_type is '���C�����:1: ����Ώۃ��C�� 0: �֘A���C��';
comment on column m_layer.layer_name is '���C����:��ʂɕ\�����郌�C����';
comment on column m_layer.table_name is '�e�[�u����:�t�B�[�`���e�[�u���i�[�e�[�u����';
comment on column m_layer.layer_code is '���C���R�[�h:GeoServer��̃��C��ID';
comment on column m_layer.layer_query is '���C���N�G��:GeoServer�Ƀ��N�G�X�g�𓊂���ۂ̃N�G��������';
comment on column m_layer.query_require_flag is '�N�G���K�{�t���O:1: �K�{ 0:�s�v';

comment on table m_label is 'M_���x��';
comment on column m_label.view_code is '��ʃR�[�h';
comment on column m_label.label_id is '���x��ID';
comment on column m_label.label_key is '���x���L�[';
comment on column m_label.label_type is '���:0:���Ǝҍs���Ƃ��Ɏg�p 1:���Ǝ҂̂ݎg�p 2:�s���̂ݎg�p';
comment on column m_label.label_text is '�e�L�X�g';
comment on column m_label.application_step is '�\���i�K:�\���i�KID�̓J���}��؂�ŕێ�
�\���i�KID���킸�A��ɕ\������ꍇ�A�uall�v�ŏ���';

comment on table o_applicant_information is 'O_�\���ҏ��';
comment on column o_applicant_information.application_id is '�\��ID';
comment on column o_applicant_information.applicant_id is '�\���ҏ��ID';
comment on column o_applicant_information.item_1 is '����1:�\���ҏ�񍀖�ID=1001�̓o�^�l���i�[';
comment on column o_applicant_information.item_2 is '����2:�\���ҏ�񍀖�ID=1002�̓o�^�l���i�[';
comment on column o_applicant_information.item_3 is '����3:�\���ҏ�񍀖�ID=1003�̓o�^�l���i�[';
comment on column o_applicant_information.item_4 is '����4:�\���ҏ�񍀖�ID=1004�̓o�^�l���i�[';
comment on column o_applicant_information.item_5 is '����5:�\���ҏ�񍀖�ID=1005�̓o�^�l���i�[';
comment on column o_applicant_information.item_6 is '����6:�\���ҏ�񍀖�ID=1006�̓o�^�l���i�[';
comment on column o_applicant_information.item_7 is '����7:�\���ҏ�񍀖�ID=1007�̓o�^�l���i�[';
comment on column o_applicant_information.item_8 is '����8:�\���ҏ�񍀖�ID=1008�̓o�^�l���i�[';
comment on column o_applicant_information.item_9 is '����9:�\���ҏ�񍀖�ID=1009�̓o�^�l���i�[';
comment on column o_applicant_information.item_10 is '����10:�\���ҏ�񍀖�ID=1010�̓o�^�l���i�[';
comment on column o_applicant_information.mail_address is '���[���A�h���X:�ʒm�Ɏg�p���郁�[���A�h���X';
comment on column o_applicant_information.collation_id is '�ƍ�ID';
comment on column o_applicant_information.password is '�p�X���[�h:�n�b�V�������Ċi�[';
comment on column o_applicant_information.contact_address_flag is '�A����t���O:1:�A���� 0:�\���ҏ��';

comment on table m_department is 'M_����';
comment on column m_department.department_id is '����ID';
comment on column m_department.department_name is '������';
comment on column m_department.mail_address is '���[���A�h���X:�ʒm��̃��[���A�h���X���J���}��؂�ŕێ�';
comment on column m_department.admin_mail_address is '�Ǘ��҃��[���A�h���X:�Y�������̊Ǘ��҂̒ʒm��̃��[���A�h���X���J���}��؂�ŕێ�';
comment on column m_department.answer_authority_flag is '�񓚌����t���O:1:��������';

comment on table o_application is 'O_�\��';
comment on column o_application.application_id is '�\��ID';
comment on column o_application.applicant_id is '�\���ҏ��ID';
comment on column o_application.status is '�X�e�[�^�X';
comment on column o_application.register_status is '�o�^�X�e�[�^�X:0: ���\���� 1: �\���ς�';
comment on column o_application.collation_text is '�ƍ�������:�{�o�^���̏ƍ�������';
comment on column o_application.register_datetime is '�o�^����';
comment on column o_application.update_datetime is '�X�V����';
comment on column o_application.application_type_id is '�\�����ID';

comment on table m_application_file is 'M_�\���t�@�C��';
comment on column m_application_file.application_file_id is '�\���t�@�C��ID';
comment on column m_application_file.judgement_item_id is '���荀��ID';
comment on column m_application_file.require_flag is '�K�{�L��:1:�K�{ 0:�C�� 2:�C��(���ӕ�������)';
comment on column m_application_file.upload_file_name is '�A�b�v���[�h�t�@�C����';
comment on column m_application_file.extension is '�g���q:���p�\�Ȋg���q���J���}��؂�';
comment on column m_application_file.application_file_type is '�\���t�@�C�����:1:�J���o�^��Ɋ܂߂�';

comment on table m_category_judgement is 'M_�敪����';
comment on column m_category_judgement.judgement_item_id is '���荀��ID';
comment on column m_category_judgement.gis_judgement is 'GIS����:0: GIS����Ȃ� 1: �d�Ȃ蔻�� 2: �d�Ȃ�Ȃ����� 3: �o�b�t�@���� 4: �o�b�t�@�d�Ȃ�Ȃ����� 5:���H����';
comment on column m_category_judgement.buffer is '�o�b�t�@:�o�b�t�@���莞�̃o�b�t�@���a (m)';
comment on column m_category_judgement.display_attribute_flag is '�d�Ȃ葮���\���t���O:0: �����\���Ȃ� 1: ��؂蕶���ŋ�؂��ĕ\�� 2:���s�\�� 3:�T���f�f���ʈꗗ�e�[�u���ōs�𕪂��ĕ\��';
comment on column m_category_judgement.judgement_layer is '����Ώۃ��C��:GIS����Ŏg�p���郌�C���̃��C��ID�i�J���}��؂�j';
comment on column m_category_judgement.table_name is '�e�[�u����:�����̃��x���Ƃ��Ďg�p����e�[�u����';
comment on column m_category_judgement.field_name is '�t�B�[���h��:�����̃��x���Ƃ��Ďg�p����t�B�[���h��';
comment on column m_category_judgement.non_applicable_layer_display_flag is '���背�C����Y�����\���L��:1:�\�� 0:��\��';
comment on column m_category_judgement.simultaneous_display_layer is '�����\�����C��:�Y���f�f���ʉ�ʂœ����\������֘A���C���̃��C��ID�i�J���}��؂�j';
comment on column m_category_judgement.simultaneous_display_layer_flag is '�����\�����C���\���L��:1:�\�� 0: ��\��';
comment on column m_category_judgement.disp_order is '�\����';

comment on table m_application_category is 'M_�\���敪';
comment on column m_application_category.category_id is '�\���敪ID';
comment on column m_application_category.view_id is '���ID';
comment on column m_application_category.order is '����';
comment on column m_application_category.label_name is '�I������';

comment on table m_application_category_selection_view is 'M_�\���敪�I�����';
comment on column m_application_category_selection_view.view_id is '���ID';
comment on column m_application_category_selection_view.view_flag is '�\���L��:1=�\��, 0=��\��';
comment on column m_application_category_selection_view.multiple_flag is '�����I��L��:1=�����I���Ȃ� 0=�����I������';
comment on column m_application_category_selection_view.require_flag is '�K�{�L��:1=�I��K�{ 0=�I��C��';
comment on column m_application_category_selection_view.title is '�^�C�g��';
comment on column m_application_category_selection_view.description is '������';
comment on column m_application_category_selection_view.judgement_type is '�\�����:1=�J������, 0=���z�m�F
�J���}��؂�Ŋi�[';

comment on table m_calendar is 'M_�J�����_';
comment on column m_calendar.cal_date is '�J�����_�[���t:���t';
comment on column m_calendar.week_day is '�j��:1:���j���A2:���j���A3:�Ηj���A4:���j���A5:�ؗj���A6;���j���A7:�y�j��';
comment on column m_calendar.biz_day_flag is '�c�Ɠ��t���O:1:�c�Ɠ��A0:��c�Ɠ�';
comment on column m_calendar.comment is '���l:���l';

comment on table o_development_document is 'O_�J���o�^��';
comment on column o_development_document.file_id is '�t�@�C��ID';
comment on column o_development_document.application_id is '�\��ID';
comment on column o_development_document.development_document_id is '�J���o�^��}�X�^ID';
comment on column o_development_document.file_path is '�t�@�C���p�X';
comment on column o_development_document.register_datetime is '�쐬����';

comment on table m_development_document is 'M_�J���o�^��';
comment on column m_development_document.development_document_id is '�J���o�^��}�X�^ID:1:�ŏI��o���� 2:�S��o���� 3:�J���o�^�� �Œ�';
comment on column m_development_document.document_name is '���ޖ�';
comment on column m_development_document.document_type is '���ގ��:1:�J���o�^��i���[�A�b�v���[�h���ɋƖ��f�[�^�����j';

comment on table o_accepting_answer is 'O_��t��:���O���c��t��';
comment on column o_accepting_answer.accepting_answer_id is '��t��ID';
comment on column o_accepting_answer.application_id is '�\��ID';
comment on column o_accepting_answer.application_step_id is '�\���i�KID';
comment on column o_accepting_answer.version_infomation is '�ŏ��';
comment on column o_accepting_answer.judgement_id is '���荀��ID';
comment on column o_accepting_answer.department_id is '����ID:���O���c�̏ꍇ�ݒ�K�{';
comment on column o_accepting_answer.judgement_result is '���茋��';
comment on column o_accepting_answer.judgement_result_index is '���茋�ʂ̃C���f�b�N�X:���ꔻ�荀�ڂ̕����s�̔��茋�ʂ̃C���f�b�N�X';
comment on column o_accepting_answer.answer_content is '�񓚓��e:�ŐV�̉񓚓��e';
comment on column o_accepting_answer.register_datetime is '�o�^����';
comment on column o_accepting_answer.update_datetime is '�X�V����';
comment on column o_accepting_answer.answer_data_type is '�f�[�^���:0:�o�^�A1:�X�V�A2�F�ǉ��A3:�s���Œǉ��A4:�ꗥ�ǉ��A
5:�폜�ς݁A6�F���p�A7:�폜�ς݁i�s���j';
comment on column o_accepting_answer.register_status is '�o�^�X�e�[�^�X:0: ���\���� 1: �\���ς�';
comment on column o_accepting_answer.deadline_datetime is '�񓚊�������';
comment on column o_accepting_answer.answer_id is '��ID:���O���c��t����O_�񓚂ɊY�����R�[�h�����݂��Ȃ��ꍇInsert���݂���ꍇUpdate';

comment on table m_applicant_information_item is 'M_�\���ҏ�񍀖�';
comment on column m_applicant_information_item.applicant_information_item_id is '�\���ҏ�񍀖�ID';
comment on column m_applicant_information_item.display_order is '����';
comment on column m_applicant_information_item.display_flag is '�\���L��';
comment on column m_applicant_information_item.require_flag is '�K�{�L��';
comment on column m_applicant_information_item.item_name is '���ږ�';
comment on column m_applicant_information_item.regex is '���K�\��:��̏ꍇ���K�\���`�F�b�N�͂��Ȃ�';
comment on column m_applicant_information_item.mail_address is '���[���A�h���X:1=���[���A�h���X';
comment on column m_applicant_information_item.search_condition_flag is '���������\���L��';
comment on column m_applicant_information_item.item_type is '���ڌ^:0:1�s�݂̂̓��͗��ŕ\���A
1:�����s�̓��͗��ŕ\���A
2:���t�i�J�����_�[�j�A
3:���l�A
4:�h���b�v�_�E���P��I��
5:�h���b�v�_�E�������I��';
comment on column m_applicant_information_item.application_step is '�\���i�K:�\���i�KID�̓J���}��؂�ŕێ�';
comment on column m_applicant_information_item.add_information_item_flag is '�ǉ����t���O:0:�\���ҏ�񍀖ځA1:�\���ǉ���񍀖�';
comment on column m_applicant_information_item.contact_address_flag is '�A����t���O:1:�A����Ƃ��ĕ\�� 0:�A����Ƃ��ĕ\�����Ȃ�';

comment on table m_ledger_label is 'M_���[���x��';
comment on column m_ledger_label.ledger_label_id is '���[���x��ID';
comment on column m_ledger_label.ledger_id is '���[�}�X�^ID';
comment on column m_ledger_label.replace_identify is '�u�����ʎq';
comment on column m_ledger_label.table_name is '�e�[�u����:�o�͂Ɏg�p����e�[�u����';
comment on column m_ledger_label.export_column_name is '�o�̓J������:�o�͂Ɏg�p����J������';
comment on column m_ledger_label.filter_column_name is '�t�B���^�J������';
comment on column m_ledger_label.filter_condition is '�t�B���^����';
comment on column m_ledger_label.item_id_1 is '����ID1';
comment on column m_ledger_label.item_id_2 is '����ID2';
comment on column m_ledger_label.convert_order is '�ϊ��I�[�_:�ϊ��Ώ�1=�ϊ��l,�ϊ��Ώ�2=�ϊ��l,�c�̃t�H�[�}�b�g�Ŏw�� 
round=x �ۂ߂錅��
dateformat=yyyy�Nmm��dd�� ���t�̏o�̓t�H�[�}�b�g
day=x �����Z�������
separate=comma ��؂蕶���i�J���}�̏ꍇcomma�j
japanese=true �a��\�����邩�ۂ�';
comment on column m_ledger_label.convert_format is '�ϊ��t�H�[�}�b�g:�ϊ���̕�����𖄂ߍ��ރt�H�[�}�b�g %s�Ŗ�����������w��';

comment on table o_department_answer is 'O_������';
comment on column o_department_answer.department_answer_id is '������ID';
comment on column o_department_answer.application_id is '�\��ID';
comment on column o_department_answer.application_step_id is '�\���i�KID';
comment on column o_department_answer.department_id is '����ID';
comment on column o_department_answer.government_confirm_status is '�s���m��X�e�[�^�X:0:���� 1:�扺 2:�p��';
comment on column o_department_answer.government_confirm_datetime is '�s���m�����';
comment on column o_department_answer.government_confirm_comment is '�s���m��R�����g';
comment on column o_department_answer.notified_text is '�ʒm�e�L�X�g:�ʒm�ς݂̍s���m��R�����g';
comment on column o_department_answer.complete_flag is '�����t���O:1: ���� 0: ������';
comment on column o_department_answer.notified_flag is '�ʒm�t���O:1:�ʒm�ς� 0:���ʒm';
comment on column o_department_answer.register_datetime is '�o�^����';
comment on column o_department_answer.update_datetime is '�X�V����';
comment on column o_department_answer.register_status is '�o�^�X�e�[�^�X:0: ���\���� 1: �\���ς�';
comment on column o_department_answer.delete_unnotified_flag is '�폜���ʒm�t���O:1:�폜�ς݁E���ʒm';
comment on column o_department_answer.government_confirm_notified_flag is '�s���m��ʒm�t���O:1:�ʒm�ς� 0:���ʒm';
comment on column o_department_answer.government_confirm_permission_flag is '�s���m��ʒm���t���O:1:���ς� 0:������';

comment on table m_judgement_authority is 'M_�敪����_����';
comment on column m_judgement_authority.judgement_item_id is '���荀��ID';
comment on column m_judgement_authority.department_id is '����ID';

comment on table o_application_version_information is 'O_�\���ŏ��';
comment on column o_application_version_information.application_id is '�\��ID';
comment on column o_application_version_information.application_step_id is '�\���i�KID';
comment on column o_application_version_information.version_information is '�ŏ��';
comment on column o_application_version_information.accepting_flag is '��t�t���O:���O���c��t��Ԃ��Ǘ�.';
comment on column o_application_version_information.accept_version_information is '��t�ŏ��:��t���ꂽ�ŏI�̔ŏ��';
comment on column o_application_version_information.register_datetime is '�o�^����';
comment on column o_application_version_information.update_datetime is '�X�V����';
comment on column o_application_version_information.complete_datetime is '��������';
comment on column o_application_version_information.register_status is '�o�^�X�e�[�^�X:0: ���\���� 1: �\���ς�';

comment on table o_applicant_information_add is 'O_�\���ǉ����';
comment on column o_applicant_information_add.applicant_id is '�\���ǉ����ID';
comment on column o_applicant_information_add.application_id is '�\��ID';
comment on column o_applicant_information_add.application_step_id is '�\���i�KID';
comment on column o_applicant_information_add.applicant_information_item_id is '�\���ҏ�񍀖�ID';
comment on column o_applicant_information_add.item_value is '���ڒl:�h���b�v�_�E�������I���̒l�̓J���}��؂�ŕێ�';
comment on column o_applicant_information_add.version_information is '�ŏ��:0: ���\���� 1: �\���ς�';

comment on table m_applicant_information_item_option is 'M_�\����񍀖ڑI����';
comment on column m_applicant_information_item_option.applicant_information_item_option_id is '�\����񍀖ڑI����ID';
comment on column m_applicant_information_item_option.applicant_information_item_id is '�\���ҏ�񍀖�ID';
comment on column m_applicant_information_item_option.display_order is '����';
comment on column m_applicant_information_item_option.applicant_information_item_option_name is '�I������';

comment on table m_authority is 'M_����';
comment on column m_authority.department_id is '����ID';
comment on column m_authority.application_step_id is '�\���i�KID';
comment on column m_authority.answer_authority_flag is '�񓚌����t���O:0: �����Ȃ� 1: ��������i���������̂ݑ���j�A2�F��������i������������j';
comment on column m_authority.notification_authority_flag is '�ʒm�����t���O:0: �����Ȃ� 1: ��������i���������̂ݑ���j�A2�F��������i������������j';

comment on table o_ledger is 'O_���[';
comment on column o_ledger.file_id is '�t�@�C��ID';
comment on column o_ledger.application_id is '�\��ID';
comment on column o_ledger.application_step_id is '�\���i�KID';
comment on column o_ledger.ledger_id is '���[�}�X�^ID';
comment on column o_ledger.file_name is '�t�@�C����';
comment on column o_ledger.file_path is '�t�@�C���p�X';
comment on column o_ledger.notify_file_path is '�ʒm�t�@�C���p�X';
comment on column o_ledger.register_datetime is '�쐬����';
comment on column o_ledger.receipt_datetime is '��̓���';
comment on column o_ledger.notify_flag is '�ʒm�t���O:1:���Ǝ҂ɒʒm�� 0:���Ǝ҂ɖ��ʒm';

comment on table m_ledger is 'M_���[';
comment on column m_ledger.ledger_id is '���[�}�X�^ID';
comment on column m_ledger.application_step_id is '�\���i�KID';
comment on column m_ledger.ledger_name is '���[��';
comment on column m_ledger.display_name is '��ʕ\����:�o�͎�ނ��P�̏ꍇ�A�\�������ݒ�K�v';
comment on column m_ledger.template_path is '�e���v���[�g�p�X';
comment on column m_ledger.output_type is '�o�͎��:0:��ɏo�́A1�F��ʂɑI�����ꂽ���R�[�h������Ώo��';
comment on column m_ledger.notification_flag is '��̎��ʒm�v��:���Ǝґ��Œ��[���_�E�����[�h���鎞�A�s���֒ʒm�v�ۂ�ێ�
0:�ʒm�s�v�A1:�ʒm�K�v';
comment on column m_ledger.ledger_type is '���[���:1:�J���o�^��Ɋ܂߂钠�[';
comment on column m_ledger.update_flag is '�X�V�t���O:1:�X�V�\ 0:�X�V�s��';
comment on column m_ledger.notify_flag is '�ʒm�t���O:1:�ʒm�K�v 0:�ʒm�s�v';
comment on column m_ledger.upload_extension is '�A�b�v���[�h���g���q';
comment on column m_ledger.information_text is '�ē��e�L�X�g';

comment on table m_application_step is 'M_�\���i�K';
comment on column m_application_step.application_step_id is '�\���i�KID';
comment on column m_application_step.application_step_name is '�\���i�K��';

comment on table m_application_type is 'M_�\�����';
comment on column m_application_type.application_type_id is '�\�����ID';
comment on column m_application_type.application_type_name is '�\����ޖ�';
comment on column m_application_type.application_step is '�\���i�K:�\���i�KID�͎��{���ŃJ���}��؂�ŕێ�
��j�J�����F�u1�F���O���k�v�@�ˁ@�u2�F���O���c�v�@�ˁ@�u3�F������v
�@�@�u1,2,3�v�Ŋi�[';

comment on table m_application_category_judgement is 'M_�\���敪_�敪����';
comment on column m_application_category_judgement.judgement_item_id is '���荀��ID';
comment on column m_application_category_judgement.view_id is '���ID';
comment on column m_application_category_judgement.category_id is '�\���敪ID';

comment on table m_judgement_result is 'M_���茋��';
comment on column m_judgement_result.judgement_item_id is '���荀��ID';
comment on column m_judgement_result.application_type_id is '�\�����ID';
comment on column m_judgement_result.application_step_id is '�\���i�KID';
comment on column m_judgement_result.department_id is '����ID:���O���c�ȊO�ł�[-1]';
comment on column m_judgement_result.title is '�^�C�g��';
comment on column m_judgement_result.answer_days is '�񓚓���';
comment on column m_judgement_result.default_answer is '�f�t�H���g��:�񓚔C�ӂ̍��ڂɓo�^���鏉���񓚕���';
comment on column m_judgement_result.applicable_summary is '�Y���\���T�v';
comment on column m_judgement_result.applicable_description is '�Y���\������';
comment on column m_judgement_result.non_applicable_display_flag is '��Y���\���L��:1:�\�� 0:��\��';
comment on column m_judgement_result.non_applicable_summary is '��Y���\���T�v';
comment on column m_judgement_result.non_applicable_description is '��Y���\������';
comment on column m_judgement_result.answer_require_flag is '�񓚕K�{�t���O:1:�񓚕K�{ 0:�񓚔C��';
comment on column m_judgement_result.answer_editable_flag is '�ҏW�\�t���O :1:�ҏW�\ 0:�ҏW�s��';

comment on table f_application_lot_number is 'F_�\���n��';
comment on column f_application_lot_number.application_id is '�\��ID';
comment on column f_application_lot_number.geom is '�W�I���g��:�}���`�|���S��.���W�n��F_�n�ԂƋ���';
comment on column f_application_lot_number.lot_numbers is '�n�Ԉꗗ';