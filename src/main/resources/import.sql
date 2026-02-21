insert into mcphttpconfig (id, name, url, authHeader)
values (1, 'example-mcp', 'http://localhost:8008/mcp/', null);

alter sequence mcphttpconfig_seq restart with 2;