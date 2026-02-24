insert into mcphttpconfig (id, name, url, authheadername, authheadervalue)
values (1, 'example-mcp', 'http://localhost:8008/mcp/', null, null);

alter sequence mcphttpconfig_seq restart with 2;