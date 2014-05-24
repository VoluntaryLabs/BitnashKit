//
//  NSString+BNJSON.m
//  BitnashKit
//
//  Created by Rich Collins on 3/11/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//


#import "NSObject+BN.h"
#import "NSString+BN.h"


@implementation NSString (BN)

- (id)asObjectFromJSONString
{
    NSError *error = nil;
    
    id object = [NSJSONSerialization JSONObjectWithData:[self dataUsingEncoding:NSUTF8StringEncoding] options:0x0 error:&error];
     
    if (error)
    {
        return nil;
    }
    else
    {
        return [object asObjectFromJSONObject];
    }
}

@end
