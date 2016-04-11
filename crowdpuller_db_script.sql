-- phpMyAdmin SQL Dump
-- version 4.1.4
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Feb 07, 2016 at 01:04 PM
-- Server version: 5.6.15-log
-- PHP Version: 5.5.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `crowdpuller`
--
CREATE DATABASE IF NOT EXISTS `crowdpuller` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `crowdpuller`;

DELIMITER $$
--
-- Procedures
--
DROP PROCEDURE IF EXISTS `AddFeedFlags`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `AddFeedFlags`(IN `mId` CHAR(36), IN `pId` CHAR(36), IN `flag` TINYINT(1) UNSIGNED)
    NO SQL
BEGIN
	If flag = 0 Then -- if Flag =0, means clear all flags
		Delete from FeedFlags where FeedFlags.mId = mId and FeedFlags.pId = pId;
	ElseIf (flag = 1 || flag = 2) Then -- if Flag=1 or 2, means add a favorite or reject
		If Not Exists(Select * from FeedFlags ff 
                      where ff.mId = mId and ff.pId = pId) Then
			Insert into FeedFlags(mId, pId, flag) Values (mId, pId, flag);
		Else 
			Update FeedFlags ff Set ff.flag = flag where ff.mId = mId and ff.pId = pId;
		End If;
	End If;
END$$

DROP PROCEDURE IF EXISTS `AddPostResponse`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `AddPostResponse`(IN `postId` CHAR(36), IN `rptrId` CHAR(36), IN `rspDtlId` INT, IN `notes` CHAR(100), OUT `errMessage` CHAR(200))
    NO SQL
    COMMENT 'Adds a post responses '
BEGIN
	SET errMessage = '';
	If Not Exists(SELECT Id FROM Posts WHERE Id=postId and active = 1 and flggd =0 and exprd=0 ) Then
		Set errMessage = '"Invalid Post."';
	End If;
	If Not Exists(SELECT Id FROM Members WHERE Id=rptrId and active=1 and blckd=0) Then
		Set errMessage = CONCAT(errMessage, ',"Invalid Member."');
	End If;
	If Not Exists(SELECT p.Id FROM Posts p 
		INNER JOIN responseType rt on p.rspType = rt.Id
		INNER JOIN responseTypeDetails rtd on rt.Id = rtd.typeId
		WHERE p.Id=postId and rtd.Id = rspDtlId) Then
		Set errMessage =  CONCAT(errMessage, ',"Invalid Response."');
	End If;

	If CHAR_LENGTH(errMessage) > 0 Then
		If Left(errMessage, 1) = "," Then
			SET errMessage = Right(errMessage, CHAR_LENGTH(errMessage) - 1);
		End If;
	ElseIf Not Exists(SELECT PId FROM postresponses WHERE postresponses.pId=postId and postresponses.rId=rptrId) Then
	   INSERT INTO postresponses(pId,rId,rspDtlId,notes,rspDt)
	   VALUES (postId,rptrId,rspDtlId,notes,CURRENT_TIMESTAMP());
	Else
	   UPDATE postresponses 
	   SET postresponses.rspDtlId=rspDtlId,postresponses.notes=notes,postresponses.rspDt=CURRENT_TIMESTAMP()
	   WHERE pId=postId and rId=rptrId;
	End If;	
END$$

DROP PROCEDURE IF EXISTS `GetFeedListForMap`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetFeedListForMap`(IN `dnelat` DECIMAL(8,6), IN `dnelng` DECIMAL(9,6), IN `dswlat` DECIMAL(8,6), IN `dswlng` DECIMAL(9,6), IN `dcatid` INT)
    NO SQL
BEGIN
	SELECT p.Id,p.hdr,lat,longi
    # ,DATE_FORMAT(p.crtdOn,'%d %b, %Y') as crtdOn
	FROM posts p 
	where p.active = 1 and p.flggd =0 and p.exprd=0 and
		(lat between dswLat and dneLat) and
		(longi between dswLng and dneLng) and
        (isnull(dcatid)=1 or catid = dcatid)
    Order by IfNull(p.modOn, p.crtdOn) desc;
END$$

DROP PROCEDURE IF EXISTS `GetFeedListForMember`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetFeedListForMember`(IN `mId` CHAR(36), IN `dcatid` INT)
    NO SQL
BEGIN
	Declare sSex Char(1);
	Declare iAge INT DEFAULT 0;
	Declare dob DATE;
	Declare dlat Decimal(8,6);
	Declare dlng Decimal(9,6);
	-- Get Member Details which might have to be matched with Post target audience
	select lat,longi,sex,bDay INTO
	dlat,dlng,sSex,dob
	From Members Where Id = mId and active = 1 and blckd = 0;
	
	If (IfNull(dob, 0) > 0) Then
		Set iAge = IfNull(Date_Format(Now(), '%Y') - Date_Format(dob, '%Y') - (Date_Format(Now(), '00-%m-%d') < Date_Format(dob, '00-%m-%d')), 0);
	else
		Set iAge = 0;
	End If;

	SELECT p.Id,p.hdr,DATE_FORMAT(p.crtdOn,'%d %b, %Y') as crtdOn, IfNull(ff.flag, 0) as flag
	#,p.Msg,p.RspType,p.ExpryDt, m.NN,m.CellNo,m.EmlId,m.ShareCI 
	FROM posts p 
		inner join Members m on p.aId = m.Id
		left join FeedFlags ff on p.Id = ff.pId and ff.mId = mId
		-- Remove if this post has been flagged by the user for which feed is generated
		left join postFlags pf on pf.pid = p.Id and pf.rId = mId
	where p.active = 1 and p.flggd =0 and p.exprd=0 and IfNull(ff.flag,0) <> 2 and
		m.active = 1 and m.blckd=0 and ISNULL(pf.pId) = 1 and
		(dlat between p.swLat and p.neLat) and
		(dlng between p.swLng and p.neLng) and
		(IfNull(p.prfSex, '')='' or sSex = p.prfSex) and
		(iAge = 0 or
			((IfNull(p.prfMinAge, 0)=0 or iAge >= p.prfMinAge) and
			(IfNull(p.prfMaxAge, 0)=0 or iAge <= p.prfMaxAge))
		) and
        (isnull(dcatid)=1 or p.catid = dcatid)
    Order by IfNull(p.modOn, p.crtdOn) desc;
END$$

DROP PROCEDURE IF EXISTS `GetPost`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetPost`(IN `pId` CHAR(36), IN `viewerId` CHAR(36), IN `IsEditMode` BIT(1))
    NO SQL
BEGIN
	-- Get Post Details for Display
	If (IsEditMode = 0) Then
		SELECT p.Id,p.hdr,p.msg,c.code as catcode,p.rspType,p.crtdOn,p.addr,
		m.FN,m.emlId,m.shareCI,IFNULL(pr.rspDtlId,0) as rspDtlId,
        ISNULL(pf.pId) as rsnFlggd
		FROM posts p inner join Members m on p.aId = m.Id
        inner join Category c on p.catid = c.id
		left join postresponses pr on pr.pid = p.Id and pr.rId = viewerId
		left join postflags pf on pf.pid = p.Id and pf.rId = m.Id
		where p.Id=pId and p.active = 1 and p.flggd =0 and p.exprd=0;
	Else-- Get Post Details For Edit
		SELECT p.*
		FROM posts p inner join Members m on p.aId = m.Id
		where p.Id=pId and p.active = 1 and p.flggd =0 and p.exprd=0;
	End If;

	-- Get the Responses options and count for this Post	
	SELECT rtd.id,rtd.ord,rtd.val,COUNT(pr.rspDtlId) as rspCount 
	FROM ResponseTypeDetails rtd 
	INNER JOIN Posts p on p.rspType = rtd.typeId 
	LEFT OUTER JOIN PostResponses pr on p.Id=pr.pId and rtd.id=pr.rspDtlId
	WHERE p.Id=pId and p.active = 1 and p.flggd =0 and p.exprd=0
	GROUP BY rtd.id,rtd.ord,rtd.val
	ORDER BY rtd.ord;
	-- Get the Flags 
	SELECT count(*) as flagCnt
	FROM Posts p
	INNER JOIN PostFlags pf on pf.pId = p.Id
	WHERE p.Id=pId and p.active = 1 and p.flggd =0 and p.exprd=0;
END$$

DROP PROCEDURE IF EXISTS `GetSessionVar`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `GetSessionVar`(IN `keyp` VARCHAR(500), OUT `valp` VARCHAR(500))
    NO SQL
Select `value` into valp
from sessionVariables sv where `key` = keyp$$

DROP PROCEDURE IF EXISTS `SetSessionVar`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `SetSessionVar`(IN `keyp` VARCHAR(500), IN `valp` VARCHAR(500))
    NO SQL
If Exists(Select `key` from sessionVariables where `key` = keyp) Then
	update sessionVariables set `value` = valp where `key` = keyp;
Else
	Insert into sessionVariables(`key`,`value`)  VALUES (keyp,valp);
End If$$

DROP PROCEDURE IF EXISTS `SigninUser`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `SigninUser`(IN `accessToken` VARCHAR(1000), IN `userType` CHAR(2), IN `usrId` VARCHAR(500), IN `email` VARCHAR(200), IN `sex` CHAR(1), IN `FN` CHAR(50), IN `expiryAt` INT)
    NO SQL
Begin
	Declare memberId char(36);
	Declare memberAddr varchar(500);
	Declare NewSignUp bit(1);
    Declare sessionId char(36);
    set memberId = null;
    set sessionId = UUID();
    
    Select Id,addr,isnull(bYr)|isnull(lat)|IsNull(longi) into 
    memberId,memberAddr,NewSignUp
    from members where emlid = email or 
		(usertype = 'FB' and fbuid = usrId) or
		(usertype = 'GP' and gpuid = usrid);
        
    if (IsNull(memberId)) Then # user does not exists
    	set memberId = UUID();
		if (usertype = 'FB') Then
			Insert into Members(Id,fbuid,FN,sex, emlid,lat,longi,crtdOn) Values
			(memberId,usrId,FN,sex,email,null,null,CURRENT_TIMESTAMP());
        ElseIf (usertype = 'GP') Then
			Insert into Members(Id,gpuid,FN,sex, emlid,lat,longi,crtdOn) Values
			(memberId,usrId,FN,sex,email,null,null,CURRENT_TIMESTAMP());
		End If;
		Set NewSignUp = 1;
    Else # user exists, then delete any existing sessions
    	Delete from Sessions where mId = memberId;
		# Set NewSignUp = 0;
    End If;
	Insert into Sessions(Id, mId, expiresAt) Values
	(sessionId, memberId, expiryAt);        
	select sessionId, memberId, NewSignUp, FN, memberAddr;
End$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `category`
--
-- Creation: Jan 20, 2016 at 04:54 PM
-- Last update: Jan 20, 2016 at 04:54 PM
--

DROP TABLE IF EXISTS `category`;
CREATE TABLE IF NOT EXISTS `category` (
  `Id` int(11) NOT NULL,
  `code` char(30) NOT NULL,
  `parent` int(11) DEFAULT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`Id`, `code`, `parent`) VALUES
(1, 'community', NULL),
(2, 'Business Promotions', NULL),
(3, 'services', NULL),
(4, 'housing', NULL),
(5, 'for sale', NULL),
(6, 'jobs', NULL),
(7, 'activities', 1),
(8, 'childcare', 1),
(9, 'classes', 1),
(10, 'events', 1),
(11, 'general', 1),
(12, 'groups', 1),
(13, 'local news', 1),
(14, 'lost/found', 1),
(15, 'rideshare', 1),
(16, 'volunteers', 1),
(17, 'Others', 1),
(18, 'Clothing', 2),
(19, 'Sports', 2),
(20, 'Food/Dining', 2),
(21, 'SuperMarkets', 2),
(22, 'Malls', 2),
(23, 'Jewelery', 2),
(24, 'General store', 2),
(25, 'Grocery store', 2),
(26, 'Hardware Store', 2),
(27, 'Pet Store', 2),
(28, 'Picture', 2),
(29, 'Shoe Store', 2),
(30, 'Toy Store', 2),
(31, 'Electronics', 2),
(32, 'Others', 2),
(33, 'automotive', 3),
(34, 'beauty', 3),
(35, 'computer', 3),
(36, 'creative', 3),
(37, 'cycle', 3),
(38, 'event', 3),
(39, 'farm+garden', 3),
(40, 'financial', 3),
(41, 'household', 3),
(42, 'labor/movers', 3),
(43, 'legal', 3),
(44, 'lessons', 3),
(45, 'Pets', 3),
(46, 'Classes', 3),
(47, 'real estate', 3),
(48, 'Healthcare', 3),
(49, 'travel/vacation', 3),
(50, 'Others', 3),
(51, 'apts / housing', 4),
(52, 'housing wanted', 4),
(53, 'office / commercial', 4),
(54, 'parking / storage', 4),
(55, 'real estate for sale', 4),
(56, 'rooms / shared', 4),
(57, 'rooms wanted', 4),
(58, 'sublets / temporary', 4),
(59, 'vacation rentals', 4),
(60, 'PG', 4),
(61, 'Others', 4),
(62, 'antiques', 5),
(63, 'appliances', 5),
(64, 'arts+crafts', 5),
(65, 'atv/utv/sno', 5),
(66, 'auto parts', 5),
(67, 'bikes', 5),
(68, 'books', 5),
(69, 'business', 5),
(70, 'cars+trucks', 5),
(71, 'cds/dvd/vhs', 5),
(72, 'cell phones', 5),
(73, 'clothes+acc', 5),
(74, 'collectibles', 5),
(75, 'computers', 5),
(76, 'electronics', 5),
(77, 'farm+garden', 5),
(78, 'free', 5),
(79, 'furniture', 5),
(80, 'garage sale', 5),
(81, 'general', 5),
(82, 'heavy equip', 5),
(83, 'household', 5),
(84, 'jewelry', 5),
(85, 'materials', 5),
(86, 'motorcycles', 5),
(87, 'music instr', 5),
(88, 'photo+video', 5),
(89, 'sporting', 5),
(90, 'tickets', 5),
(91, 'tools', 5),
(92, 'toys+games', 5),
(93, 'video gaming', 5),
(94, 'wanted', 5),
(95, 'Others', 5),
(96, 'accounting+finance', 6),
(97, 'admin / office', 6),
(98, 'arch / engineering', 6),
(99, 'art / media / design', 6),
(100, 'biotech / science', 6),
(101, 'business / mgmt', 6),
(102, 'customer service', 6),
(103, 'education', 6),
(104, 'food / bev / hosp', 6),
(105, 'general labor', 6),
(106, 'government', 6),
(107, 'human resources', 6),
(108, 'internet engineers', 6),
(109, 'legal / paralegal', 6),
(110, 'manufacturing', 6),
(111, 'marketing / pr / ad', 6),
(112, 'medical / health', 6),
(113, 'nonprofit sector', 6),
(114, 'real estate', 6),
(115, 'retail / wholesale', 6),
(116, 'sales / biz dev', 6),
(117, 'salon / spa / fitness', 6),
(118, 'security', 6),
(119, 'skilled trade / craft', 6),
(120, 'software / qa / dba', 6),
(121, 'systems / network', 6),
(122, 'technical support', 6),
(123, 'transport', 6),
(124, 'tv / film / video', 6),
(125, 'web / info design', 6),
(126, 'writing / editing', 6),
(127, 'Others', 6);

-- --------------------------------------------------------

--
-- Table structure for table `feedflags`
--
-- Creation: Feb 07, 2016 at 07:34 AM
-- Last update: Feb 07, 2016 at 07:34 AM
--

DROP TABLE IF EXISTS `feedflags`;
CREATE TABLE IF NOT EXISTS `feedflags` (
  `mId` char(36) NOT NULL COMMENT 'MemberId',
  `pId` char(36) NOT NULL COMMENT 'postId',
  `flag` tinyint(1) NOT NULL COMMENT 'flag for favorite(1) or rejected(2)',
  PRIMARY KEY (`mId`,`pId`,`flag`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='favorite or reject flag for the table';

-- --------------------------------------------------------

--
-- Table structure for table `flagreasons`
--
-- Creation: Jan 05, 2016 at 12:28 PM
-- Last update: Jan 06, 2016 at 11:47 AM
--

DROP TABLE IF EXISTS `flagreasons`;
CREATE TABLE IF NOT EXISTS `flagreasons` (
  `Id` tinyint(4) NOT NULL AUTO_INCREMENT,
  `dscrpt` char(100) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=18 ;

--
-- Dumping data for table `flagreasons`
--

INSERT INTO `flagreasons` (`Id`, `dscrpt`) VALUES
(1, 'false, misleading, deceptive, or fraudulent content'),
(2, 'offensive, obscene, defamatory, threatening, or malicious content'),
(3, 'anyones personal, identifying, confidential or proprietary information'),
(4, 'child pornography; bestiality; offers or solicitation of illegal prostitution'),
(5, 'spam; miscategorized, overposted, cross-posted, or nonlocal content'),
(6, 'Selling stolen property, property with serial number removed/altered, burglary tools, etc'),
(7, 'Selling ID cards, licenses, police insignia, government documents, birth certificates, etc'),
(8, 'Selling counterfeit, replica, or pirated items;'),
(9, 'Selling lottery or raffle tickets, gambling items'),
(10, 'affiliate marketing; network, or multi-level marketing; pyramid schemes'),
(11, 'Selling ivory; endangered, imperiled and/or protected species and any parts thereof'),
(12, 'Selling alcohol or tobacco;'),
(13, 'Selling prescription drugs, controlled substances and related items'),
(14, 'Selling weapons; firearms/guns; etc'),
(15, 'Selling ammunition, gunpowder, explosives'),
(16, 'Selling hazardous materials; body parts/fluids;'),
(17, 'any good, service, or content that violates the law or legal rights of others');

-- --------------------------------------------------------

--
-- Table structure for table `members`
--
-- Creation: Jan 18, 2016 at 01:05 PM
-- Last update: Feb 05, 2016 at 02:46 PM
--

DROP TABLE IF EXISTS `members`;
CREATE TABLE IF NOT EXISTS `members` (
  `Id` char(36) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '' COMMENT 'Member Id',
  `fbuid` varchar(500) DEFAULT NULL COMMENT 'fb userid',
  `gpuid` varchar(500) DEFAULT NULL COMMENT 'gp uid',
  `FN` char(50) DEFAULT NULL COMMENT 'Full Name',
  `bYr` smallint(4) DEFAULT NULL COMMENT 'Birth Year',
  `bDay` date DEFAULT NULL COMMENT 'Birthday',
  `sex` char(1) DEFAULT NULL COMMENT 'Gender',
  `emlId` char(200) DEFAULT NULL COMMENT 'Email Id',
  `lat` decimal(8,6) DEFAULT NULL COMMENT 'Latitude',
  `longi` decimal(9,6) DEFAULT NULL COMMENT 'Longitude',
  `addr` varchar(500) DEFAULT NULL,
  `blckd` bit(1) DEFAULT b'0' COMMENT 'Blocked',
  `shareCI` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Share Contact Info',
  `active` bit(1) NOT NULL DEFAULT b'1',
  `crtdOn` datetime NOT NULL COMMENT 'Created On',
  `modOn` datetime DEFAULT NULL COMMENT 'Modified On',
  PRIMARY KEY (`Id`),
  KEY `Latitude` (`lat`),
  KEY `Longitude` (`longi`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `members`
--

INSERT INTO `members` (`Id`, `fbuid`, `gpuid`, `FN`, `bYr`, `bDay`, `sex`, `emlId`, `lat`, `longi`, `addr`, `blckd`, `shareCI`, `active`, `crtdOn`, `modOn`) VALUES
('2ca0b4d0-ae74-11e5-b41d-5a9bfd20a6e3', '1638577586408633', NULL, 'Bitwinger Dev', 1991, NULL, 'M', 'bitwinger2014@gmail.com', '12.979225', '77.734990', 'ECC Rd, Pattandur Agrahara, Whitefield, Bengaluru, Karnataka 560066, India', b'0', b'1', b'1', '2015-12-30 03:06:10', '2016-02-07 10:54:08'),
('344ee560-b7bd-11e5-98b9-eed25222b31d', NULL, '103123671502524655764', 'Nimesh Singh', 1993, NULL, 'M', 'nimeshsingh@tesseractglobal.com', '12.979407', '77.735635', 'Pattandur Agrahara, Whitefield, Bengaluru, Karnataka, India', b'0', b'0', b'1', '2016-01-10 22:41:39', '2016-01-12 17:48:29'),
('b5b2a67c-b955-11e5-9a3f-aac4d4dbcb68', NULL, '103567618457745096595', 'PUSHPA NARAYAN', 1983, NULL, 'F', 'pushpinarayan@gmail.com', '12.979373', '77.735664', 'Pattandur Agrahara, Whitefield, Bengaluru, Karnataka, India', b'0', b'1', b'1', '2016-01-12 23:25:48', '2016-02-07 13:01:38'),
('e926f70a-cc16-11e5-b344-0de34fe075aa', '235630826770986', NULL, 'Nirmala Nim', 1958, NULL, 'F', 'nirmala0058@gmail.com', '12.983637', '77.741452', 'BTP Rd, Pattandur Agrahara, Whitefield, Bengaluru, Karnataka 560066, India', b'0', b'1', b'1', '2016-02-05 20:14:08', '2016-02-05 20:16:37');

-- --------------------------------------------------------

--
-- Table structure for table `postflags`
--
-- Creation: Feb 07, 2016 at 07:34 AM
-- Last update: Feb 07, 2016 at 07:34 AM
--

DROP TABLE IF EXISTS `postflags`;
CREATE TABLE IF NOT EXISTS `postflags` (
  `pId` char(36) NOT NULL COMMENT 'Poster''s Id',
  `rId` char(36) NOT NULL COMMENT 'Reporter''s Id',
  `flgdOn` datetime NOT NULL,
  PRIMARY KEY (`pId`,`rId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `postresponses`
--
-- Creation: Feb 07, 2016 at 07:34 AM
-- Last update: Feb 07, 2016 at 07:34 AM
--

DROP TABLE IF EXISTS `postresponses`;
CREATE TABLE IF NOT EXISTS `postresponses` (
  `pId` char(36) NOT NULL COMMENT 'PostId',
  `rId` char(36) NOT NULL COMMENT 'Responder Id',
  `rspDtlId` mediumint(9) NOT NULL COMMENT 'Response Details Id',
  `notes` char(100) DEFAULT NULL COMMENT 'Response notes',
  `rspDt` datetime NOT NULL COMMENT 'Responded Date',
  PRIMARY KEY (`pId`,`rId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `posts`
--
-- Creation: Jan 21, 2016 at 12:50 PM
-- Last update: Feb 06, 2016 at 09:16 PM
--

DROP TABLE IF EXISTS `posts`;
CREATE TABLE IF NOT EXISTS `posts` (
  `Id` char(36) NOT NULL COMMENT 'Post Id',
  `aId` char(36) NOT NULL COMMENT 'Author Id',
  `hdr` char(100) NOT NULL COMMENT 'Header',
  `msg` varchar(500) NOT NULL COMMENT 'Message',
  `catid` int(11) DEFAULT NULL,
  `rspType` int(11) DEFAULT NULL COMMENT 'Response Type',
  `lat` decimal(8,6) NOT NULL COMMENT 'Latitude',
  `longi` decimal(9,6) NOT NULL COMMENT 'Longitude',
  `addr` varchar(500) NOT NULL,
  `neLat` decimal(8,6) NOT NULL,
  `neLng` decimal(9,6) NOT NULL,
  `swLat` decimal(8,6) NOT NULL,
  `swLng` decimal(9,6) NOT NULL,
  `hghtInKM` decimal(8,3) NOT NULL COMMENT 'Length In KM',
  `wdthInKM` decimal(8,3) NOT NULL COMMENT 'Breadth In KM',
  `angle` decimal(5,2) DEFAULT NULL COMMENT 'Angle',
  `prfSex` char(1) DEFAULT NULL COMMENT 'Preferred Sex',
  `prfMinAge` tinyint(4) unsigned DEFAULT NULL COMMENT 'Preferred Min Age',
  `prfMaxAge` tinyint(4) unsigned DEFAULT NULL COMMENT 'Preferred Max Age',
  `flggd` bit(1) DEFAULT NULL COMMENT 'Flagged as InAppropriate',
  `flggdRsn` tinyint(4) unsigned DEFAULT NULL COMMENT 'Flagged Reason',
  `expryDt` datetime NOT NULL COMMENT 'Expiry Date',
  `exprd` bit(1) DEFAULT NULL COMMENT 'Expired',
  `crtdOn` datetime NOT NULL COMMENT 'Created On',
  `modOn` datetime DEFAULT NULL COMMENT 'Modified On',
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `posts`
--

INSERT INTO `posts` (`Id`, `aId`, `hdr`, `msg`, `catid`, `rspType`, `lat`, `longi`, `addr`, `neLat`, `neLng`, `swLat`, `swLng`, `hghtInKM`, `wdthInKM`, `angle`, `prfSex`, `prfMinAge`, `prfMaxAge`, `flggd`, `flggdRsn`, `expryDt`, `exprd`, `crtdOn`, `modOn`, `active`) VALUES
('e1955ed7-b214-11e5-b32d-0fee7da8d222', '2ca0b4d0-ae74-11e5-b41d-5a9bfd20a6e3', 'Hi this is a test message', 'Hi this is a test message\nHi this is a test message\nHi this is a test message\nHi this is a test message', 7, 1, '12.979415', '77.735654', 'Pattandur Agrahara, Whitefield, Bengaluru, Karnataka, India', '13.001890', '77.758720', '12.956939', '77.712589', '5.000', '5.000', NULL, NULL, NULL, NULL, b'0', NULL, '2016-02-01 00:00:00', b'1', '2016-01-03 17:54:06', '2016-02-03 11:02:56', b'1'),
('71a842c7-b63e-11e5-aac3-eddf9757eea3', '2ca0b4d0-ae74-11e5-b41d-5a9bfd20a6e3', 'How does Access-Control-Allow-Origin header work?', 'Apparently, I have completely misunderstood its semantics. I thought of something like this:\n\nA client downloads javascript code MyCode.js from http://siteA - the origin.\nThe response header of MyCode.js contains Access-Control-Allow-Origin: http://siteB, which I thought meant that MyCode.js was allowed to make cross-origin references to the site B.\nThe client triggers some functionality of MyCode.js, which in turn make requests to http://siteB, which should be fine, despite being cross-orig', 8, 1, '12.983428', '77.742310', '43, ECC Rd, Pattandur Agrahara, Whitefield, Bengaluru, Karnataka 560066, India', '12.994825', '77.756461', '12.972031', '77.728160', '2.540', '3.070', NULL, NULL, NULL, NULL, b'0', NULL, '2016-02-07 00:00:00', b'0', '2016-01-09 01:01:42', '2016-01-28 10:48:05', b'1'),
('42492956-b923-11e5-9a3f-aac4d4dbcb68', '344ee560-b7bd-11e5-98b9-eed25222b31d', 'Testing Man .. I am just Joking...', 'Testing Man .. I am just Joking...\nTesting Man .. I am just Joking...\nTesting Man .. I am just Joking...Testing Man .. I am just Joking...Testing Man .. I am just Joking...Testing Man .. I am just Joking...Testing Man .. I am just Joking...Testing Man .. I am just Joking...\nTesting Man .. I am just Joking...', 8, 2, '12.979413', '77.735637', 'Pattandur Agrahara, Whitefield, Bengaluru, Karnataka, India', '13.001889', '77.758702', '12.956937', '77.712571', '5.000', '5.000', NULL, NULL, NULL, NULL, b'0', NULL, '2016-02-11 00:00:00', b'0', '2016-01-12 17:24:39', NULL, b'1'),
('5c53b647-b9b3-11e5-a563-d88513ccca8e', '2ca0b4d0-ae74-11e5-b41d-5a9bfd20a6e3', '148 down vote favorite 29 I have a few static pages that are just pure HTML, that', 'down vote\nfavorite\n29\nI have a few static pages that are just pure HTML, that we display when the server goes down. How can I put a favicon that I made (it''s 16x16px and it''s sitting in the same directory as the HTML file; it''s called favicon.ico) as the "tab" icon as it were. I followed the rules I have read up on and no dice. I read a post that suggested the following as a link in the head section of the pages.\n\n<link rel="icon" href="favicon.ico" type="image/x-icon"/>\n<link rel="', 8, 1, '12.983566', '77.756110', 'Maithri Layout, Kadugodi, Bengaluru, Karnataka 560066, India', '12.997501', '77.793200', '12.969632', '77.719025', '3.100', '8.040', NULL, 'F', NULL, NULL, b'0', NULL, '2016-02-11 00:00:00', b'0', '2016-01-13 10:36:10', '2016-02-07 02:17:45', b'1'),
('7171fcf7-c38e-11e5-a8ab-e3b6e55a124a', '2ca0b4d0-ae74-11e5-b41d-5a9bfd20a6e3', 'resrttbfffgdfgdfgfdg', 'fb vb vcbvbvnvb bn bn nb bn', 7, 1, '12.979321', '77.735668', 'Pattandur Agrahara, Whitefield, Bengaluru, Karnataka, India', '13.001797', '77.758733', '12.956845', '77.712603', '5.000', '5.000', NULL, NULL, NULL, NULL, b'0', NULL, '2016-02-24 00:00:00', b'0', '2016-01-25 23:37:06', '2016-02-03 11:02:54', b'1');

-- --------------------------------------------------------

--
-- Table structure for table `responsetype`
--
-- Creation: Jan 05, 2016 at 12:28 PM
-- Last update: Jan 05, 2016 at 12:28 PM
--

DROP TABLE IF EXISTS `responsetype`;
CREATE TABLE IF NOT EXISTS `responsetype` (
  `Id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` char(30) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Dumping data for table `responsetype`
--

INSERT INTO `responsetype` (`Id`, `name`) VALUES
(1, 'Yes/No/Don''t Know'),
(2, 'Ratings (1-5 *)'),
(3, 'Like/Dislike/Neutral');

-- --------------------------------------------------------

--
-- Table structure for table `responsetypedetails`
--
-- Creation: Jan 05, 2016 at 12:28 PM
-- Last update: Jan 05, 2016 at 12:28 PM
--

DROP TABLE IF EXISTS `responsetypedetails`;
CREATE TABLE IF NOT EXISTS `responsetypedetails` (
  `Id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `typeId` int(10) unsigned NOT NULL COMMENT 'Response Type Id',
  `ord` tinyint(3) unsigned NOT NULL COMMENT 'Order',
  `val` char(30) NOT NULL COMMENT 'Value',
  PRIMARY KEY (`Id`),
  KEY `ResponseTypeId` (`typeId`,`ord`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `responsetypedetails`
--

INSERT INTO `responsetypedetails` (`Id`, `typeId`, `ord`, `val`) VALUES
(1, 1, 1, 'Yes'),
(2, 1, 2, 'No'),
(3, 1, 3, 'Don''t Know'),
(4, 2, 1, '*'),
(5, 2, 2, '**'),
(6, 2, 3, '***'),
(7, 2, 4, '****'),
(8, 2, 5, '*****'),
(9, 3, 1, 'Like'),
(10, 3, 2, 'Dislike'),
(11, 3, 3, 'Neutral');

-- --------------------------------------------------------

--
-- Table structure for table `sessions`
--
-- Creation: Feb 07, 2016 at 07:34 AM
-- Last update: Feb 07, 2016 at 07:34 AM
--

DROP TABLE IF EXISTS `sessions`;
CREATE TABLE IF NOT EXISTS `sessions` (
  `Id` varchar(36) NOT NULL COMMENT 'new UUId',
  `mId` char(36) NOT NULL COMMENT 'User''s Member Id',
  `expiresAt` int(10) unsigned NOT NULL COMMENT 'Unix TimeStamp',
  PRIMARY KEY (`mId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `sessionvariables`
--
-- Creation: Feb 07, 2016 at 05:44 AM
--

DROP TABLE IF EXISTS `sessionvariables`;
CREATE TABLE IF NOT EXISTS `sessionvariables` (
  `key` varchar(500) NOT NULL,
  `value` varchar(500) NOT NULL,
  PRIMARY KEY (`key`)
) ENGINE=MEMORY DEFAULT CHARSET=latin1;

--
-- Dumping data for table `sessionvariables`
--

INSERT INTO `sessionvariables` (`key`, `value`) VALUES
('fbAppAccessToken', '859675590797437|nJNNmjCSeOMeJ27aNzSepTBUY1A');

-- --------------------------------------------------------

--
-- Table structure for table `systempreferences`
--
-- Creation: Jan 05, 2016 at 12:28 PM
-- Last update: Jan 05, 2016 at 12:28 PM
--

DROP TABLE IF EXISTS `systempreferences`;
CREATE TABLE IF NOT EXISTS `systempreferences` (
  `countryCode` char(30) NOT NULL,
  `newMemFreeCredit` tinyint(3) unsigned NOT NULL,
  `refCreditsEarnPerInv` decimal(3,2) unsigned NOT NULL,
  `autoBanFlagCount` tinyint(4) unsigned NOT NULL,
  `banPostLimitForAutoBlockMember` tinyint(3) unsigned NOT NULL,
  `daysToExpirePost` smallint(6) unsigned NOT NULL,
  `minCovAreaInKM` decimal(6,2) unsigned NOT NULL,
  `maxCovAreaInKM` decimal(6,2) unsigned NOT NULL,
  PRIMARY KEY (`countryCode`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `systempreferences`
--

INSERT INTO `systempreferences` (`countryCode`, `newMemFreeCredit`, `refCreditsEarnPerInv`, `autoBanFlagCount`, `banPostLimitForAutoBlockMember`, `daysToExpirePost`, `minCovAreaInKM`, `maxCovAreaInKM`) VALUES
('ALL', 3, '0.25', 10, 5, 30, '0.50', '25.00');

DELIMITER $$
--
-- Events
--
DROP EVENT `DeleteExpiredSessions`$$
CREATE DEFINER=`root`@`localhost` EVENT `DeleteExpiredSessions` ON SCHEDULE EVERY 1 MINUTE STARTS '2015-12-27 00:00:00' ON COMPLETION PRESERVE ENABLE COMMENT 'This is an event to delete expired session' DO Begin
Delete from sessions where expiresAt <= unix_timestamp();
End$$

DROP EVENT `MarkExpiredPosts`$$
CREATE DEFINER=`root`@`localhost` EVENT `MarkExpiredPosts` ON SCHEDULE EVERY 1 DAY STARTS '2016-02-01 00:00:00' ON COMPLETION NOT PRESERVE ENABLE DO Update Posts Set exprd = 1 WHERE expryDt < CURDATE()$$

DELIMITER ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
