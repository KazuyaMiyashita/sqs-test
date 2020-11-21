create table Images(
    `id` char(36) not null,
    `url` varchar(255) not null,
    `createdAt` timestamp not null,
    primary key (`id`)
);