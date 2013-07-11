# Inserts a test user
INSERT INTO `users` (`id`, `points_in_data_count`) VALUES (0, 162);

# Inserts points for the user above
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(1, 52.57889, 13.40544, '2012-09-12 14:00:00', 0);
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(2, 52.57846, 13.40714, '2012-09-12 14:10:00', 0);
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(3, 52.57806, 13.40767, '2012-09-12 14:22:00', 0);
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(4, 52.57780, 13.40514, '2012-09-12 14:25:00', 0);
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(5, 52.57821, 13.40626, '2012-09-12 14:31:00', 0);
INSERT INTO `geo_points` (`id`, `latitude`, `longitude`, `recorded_at`, `user_id`) VALUES	(6, 52.57460, 13.40716, '2012-09-12 14:31:40', 0);