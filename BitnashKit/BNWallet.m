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
    return self;
}

- (void)setPath:(NSString *)path
{
    _server.path = path;
}

- (BNError *) error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"balance" withObject:self withArg:nil];
}

- (NSString *)createAddress
{
    return [_server sendMessage:@"createAddress" withObject:self withArg:nil];
}

- (NSString *)createPubKey
{
    return [_server sendMessage:@"createPubKey" withObject:self withArg:nil];
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

- (NSArray *)addresses
{
    return [_server sendMessage:@"addresses" withObject:self];
}

@end
