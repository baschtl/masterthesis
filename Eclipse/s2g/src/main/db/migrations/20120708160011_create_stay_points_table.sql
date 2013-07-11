CREATE TABLE `stay_points` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latitude` varchar(20) NOT NULL,
  `longitude` varchar(20) NOT NULL,
  `arr_time` datetime NOT NULL,
  `leav_time` datetime NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `i_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;