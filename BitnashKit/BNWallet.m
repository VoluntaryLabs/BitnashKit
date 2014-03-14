//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"
#import "NSObject+BNJSON.h"
#import "NSString+BNJSON.h"

@implementation BNWallet

- (id)init
{
    self = [super init];
    self.server = [[BNServer alloc] init];
    return self;
}

- (void)setPath:(NSString *)path
{
    _server.path = path;
}

- (NSError *) error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"getBalance" withObject:nil];
}

- (NSString *)createAddress
{
    return [_server sendMessage:@"createAddress" withObject:nil];
}

- (BNEscrowTx *)newEscrowTx
{
    BNEscrowTx *tx = [[BNEscrowTx alloc] init];
    tx.wallet = self;
    return tx;
}

- (NSString *)txFilePath
{
    return [_server.path stringByAppendingString:@"/tx.json"];
}

- (void)debugWriteTxFile
{
    [_server start];
    BNEscrowTx *tx = [self newEscrowTx];
    [tx fillForValue:92000];
    [[tx asJSONString] writeToFile:[self txFilePath] atomically:YES encoding:NSUTF8StringEncoding error:0x0];
}

- (BNEscrowTx *)txFromTxFile
{
    BNEscrowTx *tx = [[NSString stringWithContentsOfFile:[self txFilePath] encoding:NSUTF8StringEncoding error:0x0] asObjectFromJSONString];
    tx.wallet = self;
    return tx;
}

- (void)debugMergeWithThenSign:(BNWallet *)otherWallet
{
    BNEscrowTx *tx = [self txFromTxFile];
    [tx mergeWithEscrowTx:[otherWallet txFromTxFile]];
    [[tx asJSONString] writeToFile:[_server.path stringByAppendingString:@"/merged-tx.json"] atomically:YES encoding:NSUTF8StringEncoding error:0x0];
    [tx addFeeAndSign];
    [[tx asJSONString] writeToFile:[_server.path stringByAppendingString:@"/signed-tx.json"] atomically:YES encoding:NSUTF8StringEncoding error:0x0];
}

@end
