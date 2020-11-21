create table ImagesProcess(
    `imageId` char(36) not null,
    `succeed` boolean not null,
    `createdAt` timestamp not null,
    primary key (`imageId`),
    foreign key (imageId) references Images (id) on delete cascade on update cascade
);
