DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sku` varchar(50) NOT NULL,
  `name` varchar(256) NOT NULL,
  `description` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE(`sku`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `product_aggregation`;
CREATE TABLE `product_aggregation` (
  `product_name` varchar(50) NOT NULL,
  `count` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY(`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;