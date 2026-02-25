INSERT INTO public.session
VALUES ('2026-02-25 13:01:44.324972+00', 'dev-session');

INSERT INTO public.cortexmsoul
VALUES (1,
        'User''s name: John. Personality preference: Friendly assistant named Alexa, always ready with a good joke, ' ||
        'warm and casual tone. Use humor naturally and keep the vibe lighthearted.');
alter sequence cortexmsoul_seq restart with 2;

insert into mcphttpconfig (id, name, url, authheadername, authheadervalue)
values (1, 'example-mcp', 'http://localhost:8008/mcp/', null, null);

alter sequence mcphttpconfig_seq restart with 2;