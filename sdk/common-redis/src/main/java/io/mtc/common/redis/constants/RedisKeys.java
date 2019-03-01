package io.mtc.common.redis.constants;

/**
 * reids 键（主要是以太坊、托管账户、红包）常量类
 *
 * @author Chinhin
 * 2018/6/20
 */
public class RedisKeys {

    // 美元对人民币的汇率
    public static final String USD2CNY_RATE = "USD2CNY_RATE";

    // 加载页面
    public static final String LAUNCH_SCREEN = "LAUNCH_SCREEN";

    // app首页获得币种一览
    public static final String APP_HOME_CURRENCY = "APP_HOME_CURRENCY";

    // 以太坊的价格
    public static final String ETH_PRICE = "ETH_PRICE";

    /**
     * 以太坊代币价格
     */
    private static final String ETH_TOKEN_PRICE_PREFIX = "ETH_TOKEN_PRICE:";
    public static String ETH_TOKEN_PRICE(String tokenAddress) {
        return ETH_TOKEN_PRICE_PREFIX + tokenAddress;
    }
    private static final String ETH_TOKEN_CNY_PRICE_PREFIX = "ETH_TOKEN_CNY_PRICE:";
    public static String ETH_TOKEN_CNY_PRICE(String tokenAddress) {
        return ETH_TOKEN_CNY_PRICE_PREFIX + tokenAddress;
    }

    /**
     * 以太坊价格变化
     */
    private static final String ETH_TOKEN_CHANGE_PREFIX = "ETH_TOKEN_CHANGE:";
    public static String ETH_TOKEN_CHANGE(String tokenAddress) {
        return ETH_TOKEN_CHANGE_PREFIX + tokenAddress;
    }

    public static final String GAS_PRICE = "GAS_PRICE";


    // 以太坊区块高度，16进制
    public static final String ETH_LAST_BLOCK_NUM_HEX = "ETH_LAST_BLOCK_NUM_HEX";
    // 以太坊区块高度，10进制
    public static final String ETH_LAST_BLOCK_NUM = "ETH_LAST_BLOCK_NUM";

    // 属于平台的代币(合约地址)集合
    public static final String PLATFORM_CURRENCY_COLLECTION = "PLATFORM_CURRENCY_COLLECTION";

    /**
     * 设置缓存中余额时的区块高度。后面遍历区块时，只有高于这个高度，才会去更改这个余额
     * @param balanceKey 对应 ETH_CONTRACT_BALANCE
     * @return key
     */
    public static String SET_BALANCE_BLOCK(String balanceKey) {
        return SET_BALANCE_BLOCK_PREFIX + balanceKey;
    }
    public static final String SET_BALANCE_BLOCK_PREFIX = "SET_BALANCE_BLOCK:";

    /**
     * 多少区块高度以上时的余额才能缓存
     */
    private static final String CACHE_BALANCE_THRESHOLD_PREFIX = "CACHE_BALANCE_THRESHOLD_PREFIX:";
    public static String CACHE_BALANCE_THRESHOLD(String balanceKey) {
        return CACHE_BALANCE_THRESHOLD_PREFIX + balanceKey;
    }

    // ETH钱包余额
    public static String ETH_CONTRACT_BALANCE(String walletAddress, String contractAddress) {
        return ETH_CONTRACT_BALANCE_PREFIX + walletAddress + ":" + contractAddress;
    }
    public static final String ETH_CONTRACT_BALANCE_PREFIX = "ETH_CONTRACT_BALANCE:";

    /**
     * 以太坊托管账户用户自己的钱包地址
     */
    public static final String ETH_HOST_WALLET_ADDRESS = "ETH_HOST_WALLET_ADDRESS";

    /**
     * 以太坊托管账户用户需要监控的钱包地址的初始化是否完成
     */
    public static final String ETH_HOST_WALLET_ADDRESS_INIT_FINISH = "ETH_HOST_WALLET_ADDRESS_INIT_FINISH";

    public static String ETH_CREATE_WALLET_LOCK(Long uid, Integer currencyType) {
        return ETH_CREATE_WALLET_LOCK_PREFIX + uid + ":" + currencyType;
    }
    private static final String ETH_CREATE_WALLET_LOCK_PREFIX = "ETH_CREATE_WALLET_LOCK:";

    /**
     * 平台用户钱包地址
     */
    public static final String PLATFORM_USER_PREFIX = "PLATFORM_USER:";
    public static String PLATFORM_USER(String address) {
        return PLATFORM_USER_PREFIX + address;
    }

    private static final String CURRENCY_NAME_PREFIX = "CURRENCY_NAME:";
    /**
     * 缓存代币的名称
     * @param address 代币地址
     */
    public static String CURRENCY_NAME(String address) {
        return CURRENCY_NAME_PREFIX + address;
    }
    private static final String CURRENCY_SYMBOL_PREFIX = "CURRENCY_SYMBOL:";
    /**
     * 缓存代币的简称
     * @param address 代币地址
     */
    public static String CURRENCY_SYMBOL(String address) {
        return CURRENCY_SYMBOL_PREFIX + address;
    }

    /*################################# 区块扫描 begin ##################################################*/

    // 扫描完成的连续区块的最大值
    public static final String SCANNED_CONTINUITY_MAX = "SCANNED_CONTINUITY_MAX";

    // 扫描完成的连续区块的最小值
    public static final String SCANNED_CONTINUITY_MIN = "SCANNED_CONTINUITY_MIN";

    // 扫描的下限blockNum(这个blcokNum以下的就不用扫了)
    public static final String SCAN_LOWER_LIMIT = "SCAN_LOWER_LIMIT";

    // 扫描完成的分散的blockNum
    public static final String SCANNED_PIECE_PREFIX = "SCANNED_PIECE:";
    public static String SCANNED_PIECE(int blockNum) {
        return SCANNED_PIECE_PREFIX + blockNum;
    }

    /**
     * 扫描某个区块的锁
     * @param blockNum 区块高度
     * @return 锁名字
     */
    public static String ETH_SCAN_LOCK(int blockNum) {
        return ETH_SCAN_LOCK_PREFIX + blockNum;
    }
    public static final String ETH_SCAN_LOCK_PREFIX = "ETH_SCAN_LOCK:";

    /*################################# 区块扫描 end ##################################################*/

    /**
     * 节点上次重启的时间
     */
    private static final String ETH_ENDPOINT_PRE_REBOOT_TIME_PREFIX = "ETH_ENDPOINT_PRE_REBOOT_TIME_PREFIX:";
    public static String ETH_ENDPOINT_PRE_REBOOT_TIME(int index) {
        return ETH_ENDPOINT_PRE_REBOOT_TIME_PREFIX + index;
    }
    public static final String ETH_ENDPOINT_BLOCK_NUMBER_PREFIX = "ETH_ENDPOINT_BLOCK_NUMBER:";
    public static String ETH_ENDPOINT_BLOCK_NUMBER(int index) {
        return ETH_ENDPOINT_BLOCK_NUMBER_PREFIX + index;
    }

    public static final String SERVER_INSTANCE_INFO = "SERVER_INSTANCE_INFO";

    /*################################# 用户 start ##################################################*/
    private static final String USER_TOKEN_PREFIX = "USER_TOKEN:";
    public static String USER_TOKEN(String uid) {
        return USER_TOKEN_PREFIX + uid;
    }

    private static final String WITHDRAW_FEE_PREFIX = "WITHDRAW_FEE";
    public static String WITHDRAW_FEE(String currencyAddress) {
        return WITHDRAW_FEE_PREFIX + currencyAddress;
    }
    public static final String PENDING_WITHDRAW_PROCESS = "PENDING_WITHDRAW_PROCESS";
    public static final String PENDING_CREATE_CONTRACT_PROCESS = "PENDING_CREATE_CONTRACT_PROCESS";

    public static final String ENABLE_RED_ENVELOPE_CURRENCY = "ENABLE_ENVELOPE_CURRENCY";
    public static final String ENABLE_HOST_CURRENCY = "ENABLE_HOST_CURRENCY";

    // 红包弹窗缓存
    public static final String ENVELOPE_POP_DETAIL_PREFIX = "ENVELOPE_POP_DETAIL:";
    public static String ENVELOPE_POP_DETAIL(long envelopeId) {
        return ENVELOPE_POP_DETAIL_PREFIX + envelopeId;
    }

    // 红包发送信息缓存
    public static final String ENVELOPE_SEND_INFO_PREFIX = "ENVELOPE_SEND_INFO:";
    public static String ENVELOPE_SEND_INFO(long envelopeId) {
        return ENVELOPE_SEND_INFO_PREFIX + envelopeId;
    }

    // 抢到的用户
    public static final String ENVELOPE_GRABBED_USER_PREFIX = "ENVELOPE_GRABBED_USER:";
    public static String ENVELOPE_GRABBED_USER(long envelopeId) {
        return ENVELOPE_GRABBED_USER_PREFIX + envelopeId;
    }

    // 未抢的小红包
    private static final String UN_GRABBED_ENVELOPE_QUEUE_PREFIX = "UN_GRABBED_ENVELOPE_QUEUE:";
    public static String UN_GRABBED_ENVELOPE_QUEUE(long envelopeId) {
        return UN_GRABBED_ENVELOPE_QUEUE_PREFIX + envelopeId;
    }

    // 发币的所有分类
    public static final String CURRENCY_ALL_CATEGORIES = "CURRENCY_ALL_CATEGORIES";
    // 创建代币手续费缓存对象
    public static final String CREATE_TOKEN_FEE_OBJ = "CREATE_TOKEN_FEE_OBJ";
    // 交易中的，发币的交易hash
    public static final String CREATING_TOKEN_TXHASH = "CREATING_TOKEN_TXHASH";
    // 交易中的，发币的id对应CreateCurrency表的id
    public static final String CREATING_TOKEN_ID = "CREATING_TOKEN_ID";

    public static final String CREATE_EOS_FEE = "CREATE_EOS_FEE";

    // 合约的精度
    private static final String DECIMALS_TOKEN_PREFIX = "DECIMALS_TOKEN:";
    public static String DECIMALS_TOKEN(String tokenAddress) {
        return DECIMALS_TOKEN_PREFIX + tokenAddress;
    }

    /*####################################### 行情 ##################################### */
    public static final String QUOTATION_MARKET = "QUOTATION_MARKET";
    public static final String QUOTATION_SYMBOL = "QUOTATION_SYMBOL";
    public static final String QUOTATION_PRICE_PREFIX = "QUOTATION_PRICE_";
    public static final String QUOTATION_GUID = "QUOTATION_GUID";

    public static String QUOTATION_PRICE(String symbol){
        return QUOTATION_PRICE_PREFIX+symbol;
    }

}
