-- Les medalles donen punts: els moviments de punts poden tenir el motiu MEDALLA.
-- La referència és l'identificador de la medalla (per exemple, comarca_24_bronze), i cada medalla només es pot sumar una vegada.
alter table descobreix.moviments_punts drop constraint moviments_punts_motiu_check;
alter table descobreix.moviments_punts
    add constraint moviments_punts_motiu_check check (motiu in ('MISSIO', 'BONUS', 'DESBLOQUEIG', 'MEDALLA'));
