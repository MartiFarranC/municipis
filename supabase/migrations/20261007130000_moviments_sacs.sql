-- Els sacs donen punts quan ja no queda res per sortir: els moviments de punts poden tenir el motiu SAC.
-- La referència és l'origen del sac (per exemple, medalla_comarca_24_bronze), i cada sac només es pot obrir una vegada.
alter table descobreix.moviments_punts drop constraint moviments_punts_motiu_check;
alter table descobreix.moviments_punts
    add constraint moviments_punts_motiu_check check (motiu in ('MISSIO', 'BONUS', 'DESBLOQUEIG', 'MEDALLA', 'SAC'));
