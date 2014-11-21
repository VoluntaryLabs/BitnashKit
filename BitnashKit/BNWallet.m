//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNWallet.h"
#import "BNTx.h"
#import "NSObject+BN.h"
#import "NSString+BN.h"

@implementation BNWallet

@synthesize requiredConfirmations = _requiredConfirmations;

+ (NSArray *)jsonProperties
{
    return [NSArray array];
}

- (id)init
{
    self = [super init];
    self.server = [[BNServer alloc] init];
    self.nodeTitle = @"Wallet";
    self.nodeSuggestedWidth = 1100;
    self.shouldSortChildren = NO;
    self.nodeSubtitle = @"starting ...";
    self.transactionsNode = [[BNTransactionsNode alloc] init];
    self.transactionsNode.wallet = self;
    self.usesTestNet = YES;
    
    self.withdralNode = [[BNWithdrawl alloc] init];
    _requiredConfirmations = @1;
    [self updateActions];
    
    return self;
}

- (void)setRequiredConfirmations:(NSNumber *)requiredConfirmations
{
    _requiredConfirmations = requiredConfirmations;
    [self.server sendMessage:@"setRequiredConfirmations" withObject:self withArg:requiredConfirmations];
}

- (void)updateActions
{
    {
        NavActionSlot *slot = [self.navMirror newActionSlotWithName:@"openDepositView"];
        [slot setVisibleName:@"Deposit"];
        [slot setIsActive:self.isRunning];
        //[slot setIsVisible:NO];
        [slot.slotView syncFromSlot];
        [slot setVerifyMessage:@"This is experimental software.\n\nWe strongly recommend not storing more bitcoins than you are willing to lose in the wallet until the software is fully audited and well tested."];
        }
    
    {
        NavActionSlot *slot = [self.navMirror newActionSlotWithName:@"openWithdrawlView"];
        [slot setVisibleName:@"Widthdraw"];
        [slot setIsActive:self.isRunning && (self.balance.longLongValue > 0)];
        //[slot setIsVisible:NO];
        [slot.slotView syncFromSlot];
    }
}

- (void)delete
{
    return;
}

- (void)setNodeSubtitle:(NSString *)nodeSubtitle
{
    if (![[super nodeSubtitle] isEqualToString:nodeSubtitle])
    {
        [super setNodeSubtitle:nodeSubtitle];
        [self postParentChainChanged];
    }
}

- (void)fetch
{
    if (self.isRunning)
    {
        self.refreshInterval = 5.0;
        
        self.nodeSubtitle = [NSString stringWithFormat:@"%.4f BTC", self.balance.floatValue*0.00000001];
        
        if (self.children.count == 0)
        {
            [self setChildren:[NSMutableArray arrayWithObjects:
                               self.depositKey,
                               self.transactionsNode,
                               self.withdralNode,
                               nil]];
            
            [self setRefreshInterval:10.0];
            [self postParentChainChanged];
        }
        
        self.nodeNote = nil;
    }
    else
    {
        self.nodeSubtitle = [self status];
        NSNumber *progress = [self progress];
        if (progress)
        {
            self.nodeNote = [NSString stringWithFormat:@"%d%%",
                             (int)roundf(progress.floatValue*100)];
        }
        [self postParentChainChanged];
    }
    
    [self updateActions];
}

- (void)setPath:(NSString *)path
{
    _server.walletPath = path;
}

- (void)setCheckpointsPath:(NSString *)path
{
    _server.checkpointsPath = path;
}

- (BNError *)error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"balance" withObject:self withArg:nil];
}

- (BNKey *)createKey
{
    return [_server sendMessage:@"createKey" withObject:self withArg:nil];
}

- (BNTx *)newTx
{
    BNTx *tx = [[BNTx alloc] init];
    tx.wallet = self;
    return tx;
}

- (NSArray *)transactions
{
    NSArray *transactions = [_server sendMessage:@"transactions" withObject:self];
    
    for (BNTx *tx in transactions)
    {
        tx.wallet = self;
    }
    
    return transactions;
}

- (NSArray *)keys
{
    return [_server sendMessage:@"keys" withObject:self];
}

- (BNDepositKey *)depositKey
{
    BNDepositKey *depositKey = [[BNDepositKey alloc] init];
    [depositKey copySlotsFrom:[_server sendMessage:@"depositKey" withObject:self]];
    depositKey.bnParent = self;
    return depositKey;
}

- (NSString *)status
{
    return [_server sendMessage:@"status" withObject:self];
}

- (NSNumber *)progress
{
    return [_server sendMessage:@"progress" withObject:self];
}

- (void)setPassphrase:(NSString *)passphrase
{
    NSNumber *success = [_server sendMessage:@"setPassphrase"
                                  withObject:self
                                     withArg:passphrase];
    
    if (success.boolValue)
    {
        self.error = nil;
    }
    else
    {
        self.error = [[BNError alloc] init];
        self.error.description = @"Bad Passphrase";
    }
}

- (BOOL)isRunning
{
    if (self.server.started)
    {
        return [self.status isEqualToString:@"started"];
    }
    else
    {
        return NO;
    }
}

- (BNTx *)newWithdrawalTxToAddress:(NSString *)address withValue:(NSNumber *)value
{
    BNTx *tx = [self newTx];
    [tx payToAddress:address value:value];
    [tx addInputsAndChange];
    
    if (tx.error)
    {
        if (tx.error.insufficientValue)
        {
            [tx emptyWallet]; //TODO confirm with user first in case they mistyped?
        }
        else
        {
            [NSException raise:tx.error.description format:nil];
        }
    }
    
    [tx subtractFee];
    return tx;
}

- (BOOL)isValidAddress:(NSString *)address
{
    NSNumber *result = [self.server sendMessage:@"isValidAddress"
                                     withObject:self
                                        withArg:address];
    return result.boolValue;
}

- (BOOL)usesTestNet
{
    return self.server.usesTestNet;
}

- (void)setUsesTestNet:(BOOL)usesTestNet
{
    self.server.usesTestNet = usesTestNet;
}

@end
