local keyExists = redis.call('exists', KEYS[4])
if keyExists == 1 then
    return "message:"..KEYS[4].." is handling"
end
--消息重新投递
--Xadd test_queue * name xiaoming age 18
local msgId = ARGV[2]
if msgId or #msgId == 0 then
    msgId = "*"
end
redis.call("Xadd", KEYS[1], msgId, KEYS[3], ARGV[3])
--确认消息
--Xack test_queue test_group 1658053868394-0
redis.call("Xack", KEYS[1], KEYS[2], ARGV[1])
return "1"
