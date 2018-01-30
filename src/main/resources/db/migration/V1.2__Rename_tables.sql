alter table account change authenticationtype authentication_type varchar(8) not null;
alter table account change lastpasswordresetdate last_password_reset_date datetime;
alter table account change email email varchar(254) not null;
alter table account change password password varchar(60) not null;
alter table audit change account_email account_email varchar(254) not null;
alter table card_rating change account_email account_email varchar(254) not null;
alter table course_rating change account_email account_email varchar(254) not null;
alter table deck change synthax syntax varchar(255);
alter table deck_rating change account_email account_email varchar(254) not null;
alter table image change imagebase64 image_base64 LONGTEXT;
alter table person change imagebase64 image_base64 LONGTEXT;
alter table person change imagetype image_type varchar(6) not null;
