# Inserts a test user
INSERT INTO `users` (`id`, `points_in_data_count`) VALUES (0, 162);
INSERT INTO `users` (`id`, `points_in_data_count`) VALUES (1, 53);

# Inserts points for the user above
INSERT INTO `stay_points` (`id`, `latitude`, `longitude`, `arr_time`, `leav_time`, `user_id`) VALUES	(1, 52.57889, 13.40544, '2012-09-12 14:00:00', '2012-09-12 14:31:00', 0);
INSERT INTO `stay_points` (`id`, `latitude`, `longitude`, `arr_time`, `leav_time`, `user_id`) VALUES	(2, 52.57389, 13.40545, '2012-09-12 14:35:00', '2012-09-12 15:12:00', 0);
INSERT INTO `stay_points` (`id`, `latitude`, `longitude`, `arr_time`, `leav_time`, `user_id`) VALUES	(5, 52.57489, 13.40549, '2012-09-12 16:05:00', '2012-09-12 16:46:00', 0);
INSERT INTO `stay_points` (`id`, `latitude`, `longitude`, `arr_time`, `leav_time`, `user_id`) VALUES	(7, 52.57789, 13.40644, '2012-09-12 17:21:00', '2012-09-12 18:11:00', 0);
INSERT INTO `stay_points` (`id`, `latitude`, `longitude`, `arr_time`, `leav_time`, `user_id`) VALUES	(10, 52.57189, 13.40524, '2012-09-12 18:41:00', '2012-09-12 19:23:00', 0);