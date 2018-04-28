CREATE TABLE Account (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  first_name varchar(255) NOT NULL DEFAULT '',
  last_name varchar(255) NOT NULL DEFAULT '',
  email varchar(255) NOT NULL DEFAULT '',
  password varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE Student (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  id_account int(11) unsigned NOT NULL,
  PRIMARY KEY (id),
  KEY fk_student_account (id_account),
  CONSTRAINT fk_student_account FOREIGN KEY (id_account) REFERENCES Account (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Professor (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  id_account int(11) unsigned NOT NULL,
  title varchar(255) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_professor_account (id_account),
  CONSTRAINT fk_professor_account FOREIGN KEY (id_account) REFERENCES Account (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Course (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY course_id_index (id)
);

CREATE TABLE Assignment (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  id_course int(11) unsigned NOT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_course_assignment (id_course),
  CONSTRAINT fk_course_assignment FOREIGN KEY (id_course) REFERENCES Course (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE AssignmentSubmission (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  id_student int(11) unsigned NOT NULL,
  id_assignment int(11) unsigned NOT NULL,
  grade int(2) unsigned NOT NULL,
  time date NOT NULL,
  PRIMARY KEY (id),
  KEY fk_assignmentsubmission_student (id_student),
  KEY fk_assignmentsubmission_assignment (id_assignment),
  CONSTRAINT fk_assignmentsubmission_assignment FOREIGN KEY (id_assignment) REFERENCES Assignment (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_assignmentsubmission_student FOREIGN KEY (id_student) REFERENCES Student (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Professor_Course (
  id_course int(11) unsigned NOT NULL,
  id_professor int(11) unsigned NOT NULL,
  KEY fk_course (id_course),
  KEY fk_professor (id_professor),
  CONSTRAINT fk_course FOREIGN KEY (id_course) REFERENCES Course (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_professor FOREIGN KEY (id_professor) REFERENCES Professor (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Student_Course (
  id_student int(11) unsigned NOT NULL,
  id_course int(11) unsigned NOT NULL,
  grade int(2) DEFAULT NULL,
  KEY fk_course_1 (id_course),
  KEY fk_student (id_student),
  KEY student_course_index (id_student,id_course),
  KEY student_course_grade_index (grade),
  CONSTRAINT fk_course_1 FOREIGN KEY (id_course) REFERENCES Course (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_student FOREIGN KEY (id_student) REFERENCES Student (id) ON DELETE CASCADE ON UPDATE CASCADE
);