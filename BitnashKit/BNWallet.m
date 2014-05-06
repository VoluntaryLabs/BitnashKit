//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"
#import "BNTx.h"
#import "NSObject+BNJSON.h"
#import "NSString+BNJSON.h"

@implementation BNWallet

+ (NSArray *)jsonProperties
{
    return [NSArray array];
}

- (id)init
{
    self = [super init];
    self.server = [[BNServer alloc] init];
    self.nodeTitle = @"Wallet";
    self.nodeSuggestedWidth = 200;
    self.shouldSortChildren = NO;
    self.nodeSubtitle = @"starting ...";
    self.transactionsNode = [[BNTransactionsNode alloc] init];
    self.transactionsNode.wallet = self;
    
    self.addressesNode = [[BNKeysNode alloc] init];
    self.addressesNode.wallet = self;

    return self;
}

- (void)fetch
{
    if ([[self status] isEqualToString:@"started"])
    {
        self.nodeSubtitle = [NSString stringWithFormat:@"%.4f BTC", self.balance.floatValue*0.00000001];
        [self setChildren:[NSMutableArray arrayWithObjects:self.transactionsNode, self.addressesNode, nil]];
    }
}

- (void)setPath:(NSString *)path
{
    _server.walletPath = path;
}

- (void)setCheckpointsPath:(NSString *)path
{
    _server.checkpointsPath = path;
}

- (BNError *) error
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
    return [_server sendMessage:@"transactions" withObject:self];
}

- (NSArray *)keys
{
    return [_server sendMessage:@"keys" withObject:self];
}

- (NSString *)status
{
    return [_server sendMessage:@"status" withObject:self];
}

- (void)setPassphrase:(NSString *)passphrase
{
    [_server sendMessage:@"setPassphrase" withObject:self withArg:passphrase];
}

@end
