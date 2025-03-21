-- ###############################################
-- ### シーケンス生成用SQL 
-- ###############################################
-- 申請IDシーケンス
DROP SEQUENCE IF EXISTS seq_application;
CREATE SEQUENCE seq_application
    INCREMENT 1
    START 1;

-- 申請者情報IDシーケンス
DROP SEQUENCE IF EXISTS seq_applicant;
CREATE SEQUENCE seq_applicant
    INCREMENT 1
    START 1;

-- 照合IDシーケンス 
DROP SEQUENCE IF EXISTS seq_collation;
CREATE SEQUENCE seq_collation
    INCREMENT 1
    START 1;

-- 回答IDシーケンス 
DROP SEQUENCE IF EXISTS seq_answer;
CREATE SEQUENCE seq_answer
    INCREMENT 1
    START 1;

-- 回答ファイルIDシーケンス 
DROP SEQUENCE IF EXISTS seq_answer_file;
CREATE SEQUENCE seq_answer_file
    INCREMENT 1
    START 1;

-- ファイルIDシーケンス 
DROP SEQUENCE IF EXISTS seq_file;
CREATE SEQUENCE seq_file
    INCREMENT 1
    START 1;
-- 概況診断結果IDシーケンス
DROP SEQUENCE IF EXISTS seq_general_condition_diagnosis;
CREATE SEQUENCE seq_general_condition_diagnosis
    INCREMENT 1
    START 1;
-- 回答ファイル履歴IDシーケンス
DROP SEQUENCE IF EXISTS seq_answer_file_history;
CREATE SEQUENCE seq_answer_file_history
    INCREMENT 1
    START 1;
-- 回答履歴IDシーケンス
DROP SEQUENCE IF EXISTS seq_answer_history;
CREATE SEQUENCE seq_answer_history
    INCREMENT 1
    START 1;
-- チャットIDシーケンス
DROP SEQUENCE IF EXISTS seq_chat;
CREATE SEQUENCE seq_chat
    INCREMENT 1
    START 1;
-- メッセージIDシーケンス
DROP SEQUENCE IF EXISTS seq_message;
CREATE SEQUENCE seq_message
    INCREMENT 1
    START 1;
-- アクセスIDのシーケンス
DROP SEQUENCE IF EXISTS seq_access_id;
CREATE SEQUENCE seq_access_id
    INCREMENT 1 
    START 1;

-- 問合せファイルID
DROP SEQUENCE IF EXISTS seq_inquiry_file;
CREATE SEQUENCE seq_inquiry_file
    INCREMENT 1
    START 1;

-- 問合せ宛先ID
DROP SEQUENCE IF EXISTS seq_inquiry_address;
CREATE SEQUENCE seq_inquiry_address
    INCREMENT 1
    START 1;
